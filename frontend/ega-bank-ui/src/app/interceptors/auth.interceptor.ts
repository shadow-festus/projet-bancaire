import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, filter, switchMap, take } from 'rxjs/operators';
import { AuthResponse } from '../models/auth.models';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);
  private authService: AuthService | null = null;

  constructor(private injector: Injector, private router: Router) { }

  // Chargement lazy de AuthService pour éviter la dépendance circulaire
  private getAuthService(): AuthService {
    if (!this.authService) {
      this.authService = this.injector.get(AuthService);
    }
    return this.authService;
  }

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Ne pas ajouter de token pour les endpoints d'authentification
    if (this.isAuthEndpoint(req.url)) {
      return next.handle(req);
    }

    const token = localStorage.getItem('accessToken');
    let authReq = req;

    if (token) {
      authReq = this.addTokenToRequest(req, token);
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        console.log('[AuthInterceptor] Erreur HTTP:', error.status, error.url);

        if (error.status === 401 && !this.isAuthEndpoint(req.url)) {
          console.log('[AuthInterceptor] Tentative de gestion de l\'erreur 401...');
          return this.handle401Error(authReq, next);
        }
        return throwError(() => error);
      })
    );
  }

  private addTokenToRequest(request: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  private isAuthEndpoint(url: string): boolean {
    return url.includes('/auth/login') ||
      url.includes('/auth/register') ||
      url.includes('/auth/refresh');
  }

  private handle401Error(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      const refreshToken = localStorage.getItem('refreshToken');

      if (refreshToken) {
        console.log('[AuthInterceptor] Tentative de rafraîchissement du token...');
        return this.getAuthService().refreshToken(refreshToken).pipe(
          switchMap((response: AuthResponse) => {
            this.isRefreshing = false;
            console.log('[AuthInterceptor] Réponse de rafraîchissement du token:', response);

            if (response?.accessToken) {
              localStorage.setItem('accessToken', response.accessToken);
              if (response.refreshToken) {
                localStorage.setItem('refreshToken', response.refreshToken);
              }
              this.refreshTokenSubject.next(response.accessToken);
              console.log('[AuthInterceptor] Token rafraîchi avec succès, relance de la requête');
              return next.handle(this.addTokenToRequest(request, response.accessToken));
            }

            // Si pas de token dans la réponse, déconnecter
            console.log('[AuthInterceptor] Pas de token dans la réponse, déconnexion');
            this.handleLogout();
            return throwError(() => new Error('Échec du rafraîchissement du token'));
          }),
          catchError((err) => {
            this.isRefreshing = false;
            console.error('[AuthInterceptor] Échec du rafraîchissement du token:', err);
            this.handleLogout();
            return throwError(() => err);
          })
        );
      } else {
        // Pas de refresh token, déconnecter
        console.log('[AuthInterceptor] Pas de refresh token disponible, déconnexion');
        this.isRefreshing = false;
        this.handleLogout();
        return throwError(() => new Error('Pas de refresh token disponible'));
      }
    }

    // Une autre requête est déjà en train de rafraîchir le token
    // Attendre que le nouveau token soit disponible
    console.log('[AuthInterceptor] En attente du rafraîchissement du token par une autre requête...');
    return this.refreshTokenSubject.pipe(
      filter((token) => token !== null),
      take(1),
      switchMap((token) => next.handle(this.addTokenToRequest(request, token!)))
    );
  }

  private handleLogout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    // Sauvegarder l'URL actuelle pour redirection après login
    const currentUrl = this.router.url;
    if (currentUrl && currentUrl !== '/login' && currentUrl !== '/register') {
      this.router.navigate(['/login'], { queryParams: { returnUrl: currentUrl, expired: 'true' } });
    } else {
      this.router.navigate(['/login']);
    }
  }
}
