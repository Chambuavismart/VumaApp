# Identity Engine Architecture

## 1. Overview
The Canonical Identity Engine in VumaPhaseTwo ensures that external football data from various providers (such as Football-Data.co.uk, Opta, Wyscout, or official league feeds) can be deterministically resolved to stable internal canonical entities for:
- Teams (`teams` table)
- Competitions (`competitions` table)
- Seasons (`seasons` and `competition_seasons` tables)

The engine rejects unrestricted string similarity or guessing. Entity resolution requires an explicit alias mapping or an unambiguous direct match on normalized names within the verified geographic/competition context.

---

## 2. Normalization Engine (`IdentityNormalizationService`)
Deterministic string normalization guarantees that superficial variances (casing, trailing spaces, multiple spaces, diacritics, and punctuation noise) produce consistent keys without modifying the underlying raw aliases:

1. **Unicode NFD & Diacritics Stripping:** Decomposes characters like `Atlético` -> `Atletico`, `München` -> `Munchen`.
2. **Apostrophes & Hyphens:** Standardizes curly/backtick apostrophes (`’`, `` ` ``, `´`) to `'` and en/em dashes (`–`, `—`, `−`) to `-`.
3. **Punctuation Clean-up:** Replaces dots, commas, parentheses, and brackets with spaces.
4. **Casing & Whitespace:** Standardizes to lowercase with `Locale.ROOT` and compresses multiple whitespace characters into a single space.
5. **Harmless Suffix Handling:** Recognizes optional suffixes such as `FC`, `AFC`, `Calcio`, `Football Club` without deleting core team names.
6. **Season Code Standardization:** Normalizes `2024/25`, `2024-2025`, `2024-25`, `24/25` to `2024/2025`, or `2024` for calendar seasons.

---

## 3. Team Resolution Waterfall
Resolution of incoming team names follows a prioritized, auditable sequence:

1. **Validation:** Checks for null or blank input, returning `INVALID_INPUT` if empty.
2. **Exact Scoped Alias:** Searches `team_aliases` matching `(source, normalized_alias, country_context, competition_context)`.
3. **Broad Provider Alias:** Searches `team_aliases` matching `(source, normalized_alias)`. If multiple canonical teams match without sufficient context, returns `AMBIGUOUS`.
4. **Canonical Direct Match:** Searches `teams` matching `normalized_name`. If filtered by `country_context` and unique, resolves cleanly; if multiple countries match, returns `AMBIGUOUS` with all candidates.
5. **Cross-Provider Alias Lookup:** Evaluates aliases across all providers with context filtering.
6. **Unresolved:** Returns `UNRESOLVED` with diagnostic details if no verified mapping exists.

---

## 4. Competition Resolution & Source Code Mapping
Provider-specific competition codes such as Football-Data.co.uk codes (`E0` = English Premier League, `E1` = Championship, `D1` = Bundesliga, `I1` = Serie A, `SP1` = La Liga) are stored as aliases in `competition_aliases`:
- `source`: e.g. `football-data`
- `source_code`: e.g. `E0`
- `raw_alias`: `E0`
- `normalized_alias`: `e0`
- `country_context`: `england`

When an incoming match arrives with `source = 'football-data'` and competition `E0`, it resolves directly to canonical `Premier League` (England).

---

## 5. Season Resolution & Boundary Integrity
Season resolution adheres to competition-specific calendar boundaries:
- Supports cross-year seasons (e.g., August to May in European leagues) and single calendar-year seasons.
- Date-based resolution verifies that a fixture date falls within `[competition_seasons.start_date, competition_seasons.end_date]`.
- Composite foreign key constraints (`fk_matches_comp_season_triplet`) enforce that `(competition_season_id, competition_id, season_id)` must always refer to the identical valid relationship.

---

## 6. Alias Management & Uniqueness Constraints
1. **Conflict Detection:** Attempting to register an alias that is already mapped to another canonical entity within the same scope throws an HTTP 409 Conflict.
2. **Idempotence:** Registering an existing alias against the same canonical entity returns the existing record without error.
3. **Database Functional Indexes:** Active indexes in `V8` use `COALESCE` to guarantee uniqueness even with nullable context columns:
   - `team_aliases`: `(source, normalized_alias, (COALESCE(country_context, '')), (COALESCE(competition_context, '')))`
   - `competition_aliases`: `(source, normalized_alias, (COALESCE(country_context, '')))`

---

## 7. Rescheduled Fixture Design Considerations
Natural match keys format as `[COUNTRY]-[COMP]-[SEASON]-[HOME]-[AWAY]-[YYYYMMDD]`.
- Rescheduled fixtures receive a new date, altering the natural key.
- To prevent duplicate insertion during Phase 3 ingestion, the ingestion engine must match historical fixtures by canonical `(home_team_id, away_team_id, competition_season_id)` and match round, prompting an update to the match date and natural key rather than an accidental second insertion.
