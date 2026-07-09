// auth.service.ts
// Angular service for authentication — manages tokens and user state

import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, UserResponse } from '../models/auth.model';

const TOKEN_KEY = 'auth_token';
const USER_KEY  = 'auth_user';
const API_BASE  = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class AuthService {

  /** Reactive signal — components can read currentUser() directly */
  readonly currentUser = signal<UserResponse | null>(this.loadStoredUser());

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  /**
   * POST /api/auth/login
   * Stores the JWT and user info on success.
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_BASE}/auth/login`, credentials)
      .pipe(
        tap(response => {
          this.storeToken(response.token);
          const user: UserResponse = {
            id: 0,
            matricule: response.matricule,
            structure: response.structure,
            role: response.role,
            enabled: true,
          };
          this.storeUser(user);
          this.currentUser.set(user);
        })
      );
  }

  /** Clears stored credentials and redirects to login. */
  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  /** Returns the stored JWT string, or null if not authenticated. */
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  /** True when a JWT token is present in storage. */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /** Returns true if the current user has the given role. */
  hasRole(role: string): boolean {
    return this.currentUser()?.role === role;
  }

  // ---------------------------------------------------------------- Private

  private storeToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  private storeUser(user: UserResponse): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  private loadStoredUser(): UserResponse | null {
    const stored = localStorage.getItem(USER_KEY);
    return stored ? JSON.parse(stored) : null;
  }
}
