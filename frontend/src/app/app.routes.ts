import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { MatchesHubComponent } from './components/matches/matches-hub.component';
import { TeamExplorerComponent } from './components/identity/team-explorer.component';
import { CompetitionsHubComponent } from './components/competitions/competitions-hub.component';
import { IngestionCentreComponent } from './components/ingestion/ingestion-centre.component';
import { ResearchLabComponent } from './components/research-lab/research-lab.component';
import { SystemDiagnosticsComponent } from './components/diagnostics/system-diagnostics.component';
import { IdentityDashboardComponent } from './components/identity/identity-dashboard.component';
import { DatabaseExplorerComponent } from './components/database-explorer/database-explorer.component';
import { ResolutionConsoleComponent } from './components/identity/resolution-console.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  // --- PRIMARY FOOTBALL RESEARCH DESTINATIONS ---
  {
    path: 'dashboard',
    component: DashboardComponent
  },
  {
    path: 'matches',
    component: MatchesHubComponent
  },
  {
    path: 'teams',
    component: TeamExplorerComponent
  },
  {
    path: 'competitions',
    component: CompetitionsHubComponent
  },
  {
    path: 'data-centre',
    component: IngestionCentreComponent
  },
  {
    path: 'research-lab',
    component: ResearchLabComponent
  },
  // --- SECONDARY TECHNICAL DESTINATION ---
  {
    path: 'system-diagnostics',
    component: SystemDiagnosticsComponent
  },

  // --- BACKWARD COMPATIBLE ALIAS ROUTES ---
  {
    path: 'database-explorer',
    component: DatabaseExplorerComponent
  },
  {
    path: 'identity-management',
    component: IdentityDashboardComponent
  },
  {
    path: 'team-registry',
    component: TeamExplorerComponent
  },
  {
    path: 'competition-registry',
    component: CompetitionsHubComponent
  },
  {
    path: 'season-registry',
    component: CompetitionsHubComponent
  },
  {
    path: 'resolution-console',
    component: ResolutionConsoleComponent
  },
  {
    path: 'alias-resolution',
    component: ResolutionConsoleComponent
  },
  {
    path: 'data-import',
    component: IngestionCentreComponent
  },
  {
    path: 'import-history',
    component: IngestionCentreComponent
  },
  {
    path: 'quarantine',
    component: IngestionCentreComponent
  },
  {
    path: 'historical-matches',
    component: MatchesHubComponent
  },
  {
    path: 'data-quality',
    component: ResearchLabComponent
  },
  {
    path: 'system-verification',
    component: ResearchLabComponent
  },
  {
    path: 'historical-data',
    component: ResearchLabComponent
  },
  {
    path: 'settings',
    component: SystemDiagnosticsComponent
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
