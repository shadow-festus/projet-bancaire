import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AuthResponse, LoginPayload, RegisterPayload, UserInfo } from '../models/auth.models';
import { AppStore } from '../stores/app.store';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(
    private api: ApiService,
    private router: Router,
    private store: AppStore
  ) { }

  register(payload: RegisterPayload): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/auth/register', payload).pipe(
      tap((res: AuthResponse) => {
        if (res?.accessToken) localStorage.setItem('accessToken', res.accessToken);
        if (res?.refreshToken) localStorage.setItem('refreshToken', res.refreshToken);
        if (res?.username) localStorage.setItem('username', res.username);
        if (res?.email) localStorage.setItem('email', res.email);
        if (res?.clientId) localStorage.setItem('clientId', res.clientId.toString());
        if (res?.role) localStorage.setItem('role', res.role);
      })
    );
  }

  login(payload: LoginPayload): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/auth/login', payload).pipe(
      tap((res: AuthResponse) => {
        if (res?.accessToken) localStorage.setItem('accessToken', res.accessToken);
        if (res?.refreshToken) localStorage.setItem('refreshToken', res.refreshToken);
        if (res?.clientId) localStorage.setItem('clientId', res.clientId.toString());
        if (res?.role) localStorage.setItem('role', res.role);
        if (res?.username) localStorage.setItem('username', res.username);
        if (res?.email) localStorage.setItem('email', res.email);
      })
    );
  }

  refreshToken(refreshToken: string): Observable<AuthResponse> {
    return this.api.post<AuthResponse>(`/auth/refresh?refreshToken=${encodeURIComponent(refreshToken)}`, null);
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('clientId');
    localStorage.removeItem('clientId');
    localStorage.removeItem('role');
    localStorage.removeItem('username');
    localStorage.removeItem('email');

    // Réinitialiser le store pour nettoyer l'état
    this.store.reset();

    this.router.navigateByUrl('/login');
  }

  /**
   * Vérifie si l'utilisateur est authentifié en validant:
   * 1. La présence d'un token
   * 2. La validité du token (non expiré)
   */
  isAuthenticated(): boolean {
    const token = localStorage.getItem('accessToken');
    if (!token) return false;

    // Vérifier si le token n'est pas expiré
    return !this.isTokenExpired(token);
  }

  /**
   * Vérifie si un token JWT est expiré
   */
  private isTokenExpired(token: string): boolean {
    try {
      const payload = this.decodeToken(token);
      if (!payload || !payload.exp) {
        return true; // Si pas d'expiration, considérer comme expiré
      }

      // exp est en secondes, Date.now() en millisecondes
      const expirationDate = payload.exp * 1000;
      const now = Date.now();

      // Ajouter une marge de 30 secondes pour éviter les problèmes de timing
      return now >= (expirationDate - 30000);
    } catch {
      return true; // En cas d'erreur de décodage, considérer comme expiré
    }
  }

  /**
   * Décode le payload d'un token JWT
   */
  private decodeToken(token: string): { sub?: string; exp?: number } | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        return null;
      }

      const payload = parts[1];
      // Décoder le Base64URL en Base64 standard
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );

      return JSON.parse(jsonPayload);
    } catch {
      return null;
    }
  }

  /**
   * Récupère les informations de l'utilisateur depuis le token
   */
  getUserInfo(): UserInfo | null {
    const token = localStorage.getItem('accessToken');
    if (!token) return null;

    const payload = this.decodeToken(token);
    if (!payload) return null;

    return {
      username: payload.sub || '',
      exp: payload.exp || 0,
    };
  }

  /**
   * Vérifie si un refresh token est disponible
   */
  hasRefreshToken(): boolean {
    return !!localStorage.getItem('refreshToken');
  }

  /**
   * Récupère l'ID du client connecté (si disponible)
   */
  getClientId(): number | null {
    const id = localStorage.getItem('clientId');
    return id ? parseInt(id, 10) : null;
  }

  /**
   * Récupère le rôle de l'utilisateur connecté
   */
  getUserRole(): string | null {
    return localStorage.getItem('role');
  }

  getUsername(): string {
    return localStorage.getItem('username') || 'Utilisateur';
  }

  getEmail(): string {
    return localStorage.getItem('email') || '';
  }
}
