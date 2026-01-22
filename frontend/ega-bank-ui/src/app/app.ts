import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { AppSidebar } from './shared/app-sidebar.component';
import { DashboardHeader } from './shared/dashboard-header.component';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, AppSidebar, DashboardHeader],
  templateUrl: './app.html',
  // Rely on global styles.css for theming and utilities
})
export class App {
  protected readonly title = signal('angular-app');
  showLayout = signal(true);

  constructor(private router: Router) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        const url = event.urlAfterRedirects.split('?')[0];
        this.showLayout.set(!['/login', '/register'].includes(url));
      }
    });
  }
}
