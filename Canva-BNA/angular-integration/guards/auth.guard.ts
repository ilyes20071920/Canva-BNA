// auth.guard.ts
// Route Guards — protect routes based on authentication and role

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard: allows access only to authenticated users.
 *
 * Usage in route config:
 * ```ts
 * { path: 'profile', component: ProfileComponent, canActivate: [authGuard] }
 * ```
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }
  return router.createUrlTree(['/login']);
};

/**
 * Guard factory: allows access only to users with the specified role.
 *
 * Usage in route config:
 * ```ts
 * { path: 'admin', component: AdminComponent, canActivate: [roleGuard('ROLE_ADMIN')] }
 * ```
 */
export const roleGuard = (requiredRole: string): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/login']);
    }

    if (authService.hasRole(requiredRole)) {
      return true;
    }

    // Authenticated but wrong role — redirect to forbidden page
    return router.createUrlTree(['/forbidden']);
  };
};

// ----------------------------------------------------------------
// Example app.routes.ts integration
// ----------------------------------------------------------------
/*
import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'forbidden', loadComponent: () => import('./pages/forbidden/forbidden.component').then(m => m.ForbiddenComponent) },

  // Any authenticated user
  { path: 'profile', loadComponent: () => import('./pages/profile/profile.component').then(m => m.ProfileComponent),
    canActivate: [authGuard] },

  // Admin only
  { path: 'admin', loadComponent: () => import('./pages/admin/admin.component').then(m => m.AdminComponent),
    canActivate: [roleGuard('ROLE_ADMIN')] },

  // Prise en charge only
  { path: 'prise-en-charge', loadComponent: () => import('./pages/prise-en-charge/prise-en-charge.component').then(m => m.PriseEnChargeComponent),
    canActivate: [roleGuard('ROLE_PRISE_EN_CHARGE')] },

  // Chef de division only
  { path: 'chef-division', loadComponent: () => import('./pages/chef-division/chef-division.component').then(m => m.ChefDivisionComponent),
    canActivate: [roleGuard('ROLE_CHEF_DIVISION')] },

  { path: '', redirectTo: 'profile', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' },
];
*/
