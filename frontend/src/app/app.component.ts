import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { HeaderComponent } from './components/header/header.component';
import { SystemService } from './services/system.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent, HeaderComponent],
  template: `
    <div class="app-layout">
      <!-- Sidebar Navigation -->
      <app-sidebar></app-sidebar>

      <!-- Main Content Area -->
      <div class="main-wrapper">
        <app-header></app-header>
        <main class="content-area">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .app-layout {
      display: flex;
      width: 100vw;
      height: 100vh;
      overflow: hidden;
      background-color: #0a0e14;
    }

    .main-wrapper {
      flex: 1;
      display: flex;
      flex-direction: column;
      height: 100vh;
      min-width: 0;
      overflow: hidden;
    }

    .content-area {
      flex: 1;
      overflow-y: auto;
      background: #0a0e14;
    }
  `]
})
export class AppComponent implements OnInit {
  private systemService = inject(SystemService);

  ngOnInit() {
    this.systemService.fetchStatus().subscribe();
  }
}
