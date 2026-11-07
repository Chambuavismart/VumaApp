package com.VumaPhaseTwo.service.ingestion;

import com.VumaPhaseTwo.dto.ingestion.FootballDataSourceOption;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FootballDataSourceCatalogService {

    public List<FootballDataSourceOption> getVerifiedSources() {
        List<FootballDataSourceOption> sources = new ArrayList<>();

        // English Premier League (E0)
        sources.add(FootballDataSourceOption.builder()
                .competitionCode("E0")
                .competitionName("Premier League")
                .country("England")
                .distributionFormat("SEASON_SPECIFIC")
                .seasonLabel("2024/2025")
                .seasonCode("2425")
                .downloadUrl("https://www.football-data.co.uk/mmz4281/2425/E0.csv")
                .verified(true)
                .notes("Official verified single-season download for Premier League 2024/2025")
                .build());

        sources.add(FootballDataSourceOption.builder()
                .competitionCode("E0")
                .competitionName("Premier League")
                .country("England")
                .distributionFormat("SEASON_SPECIFIC")
                .seasonLabel("2023/2024")
                .seasonCode("2324")
                .downloadUrl("https://www.football-data.co.uk/mmz4281/2324/E0.csv")
                .verified(true)
                .notes("Official verified single-season download for Premier League 2023/2024")
                .build());

        sources.add(FootballDataSourceOption.builder()
                .competitionCode("E0")
                .competitionName("Premier League")
                .country("England")
                .distributionFormat("SEASON_SPECIFIC")
                .seasonLabel("2022/2023")
                .seasonCode("2223")
                .downloadUrl("https://www.football-data.co.uk/mmz4281/2223/E0.csv")
                .verified(true)
                .notes("Official verified single-season download for Premier League 2022/2023")
                .build());

        // English Championship (E1)
        sources.add(FootballDataSourceOption.builder()
                .competitionCode("E1")
                .competitionName("Championship")
                .country("England")
                .distributionFormat("SEASON_SPECIFIC")
                .seasonLabel("2024/2025")
                .seasonCode("2425")
                .downloadUrl("https://www.football-data.co.uk/mmz4281/2425/E1.csv")
                .verified(true)
                .notes("Official verified single-season download for Championship 2024/2025")
                .build());

        // German Bundesliga (D1)
        sources.add(FootballDataSourceOption.builder()
                .competitionCode("D1")
                .competitionName("Bundesliga")
                .country("Germany")
                .distributionFormat("SEASON_SPECIFIC")
                .seasonLabel("2024/2025")
                .seasonCode("2425")
                .downloadUrl("https://www.football-data.co.uk/mmz4281/2425/D1.csv")
                .verified(true)
                .notes("Official verified single-season download for Bundesliga 2024/2025")
                .build());

        // Spanish La Liga (SP1)
        sources.add(FootballDataSourceOption.builder()
                .competitionCode("SP1")
                .competitionName("La Liga")
                .country("Spain")
                .distributionFormat("SEASON_SPECIFIC")
                .seasonLabel("2024/2025")
                .seasonCode("2425")
                .downloadUrl("https://www.football-data.co.uk/mmz4281/2425/SP1.csv")
                .verified(true)
                .notes("Official verified single-season download for La Liga 2024/2025")
                .build());

        // Italian Serie A (I1)
        sources.add(FootballDataSourceOption.builder()
                .competitionCode("I1")
                .competitionName("Serie A")
                .country("Italy")
                .distributionFormat("SEASON_SPECIFIC")
                .seasonLabel("2024/2025")
                .seasonCode("2425")
                .downloadUrl("https://www.football-data.co.uk/mmz4281/2425/I1.csv")
                .verified(true)
                .notes("Official verified single-season download for Serie A 2024/2025")
                .build());

        // French Ligue 1 (F1)
        sources.add(FootballDataSourceOption.builder()
                .competitionCode("F1")
                .competitionName("Ligue 1")
                .country("France")
                .distributionFormat("SEASON_SPECIFIC")
                .seasonLabel("2024/2025")
                .seasonCode("2425")
                .downloadUrl("https://www.football-data.co.uk/mmz4281/2425/F1.csv")
                .verified(true)
                .notes("Official verified single-season download for Ligue 1 2024/2025")
                .build());

        // Stacked / Special Multi-Season Datasets
        sources.add(FootballDataSourceOption.builder()
                .competitionCode("E0_STACKED")
                .competitionName("Premier League (Historical Multi-Season Stacked)")
                .country("England")
                .distributionFormat("STACKED_MULTI_SEASON")
                .seasonLabel("Multi-Season (Stacked 2016-2025)")
                .seasonCode("STACKED")
                .downloadUrl("https://www.football-data.co.uk/data/E0_all.csv")
                .verified(true)
                .notes("Verified stacked CSV distribution containing multi-season match records")
                .build());

        sources.add(FootballDataSourceOption.builder()
                .competitionCode("D1_STACKED")
                .competitionName("Bundesliga (Historical Multi-Season Stacked)")
                .country("Germany")
                .distributionFormat("STACKED_MULTI_SEASON")
                .seasonLabel("Multi-Season (Stacked 2016-2025)")
                .seasonCode("STACKED")
                .downloadUrl("https://www.football-data.co.uk/data/D1_all.csv")
                .verified(true)
                .notes("Verified stacked CSV distribution containing multi-season match records")
                .build());

        return sources;
    }
}
