import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent),
    children: [
      {
        path: 'users',
        loadComponent: () => import('./dashboard/user-management/user-management.component').then(m => m.UserManagementComponent)
      },
      {
        path: 'fiche-client',
        loadComponent: () => import('./dashboard/fiche-client/fiche-client.component').then(m => m.FicheClientComponent)
      },
      {
        path: 'liste-decision',
        loadComponent: () => import('./dashboard/liste-decision/liste-decision.component').then(m => m.ListeDecisionComponent)
      },
      { path: '', redirectTo: 'users', pathMatch: 'full' }
    ]
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
