import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    // Vérifier si l'utilisateur est authentifié (token valide et non expiré)
    if (this.auth.isAuthenticated()) {
      return true;
    }

    // Si l'utilisateur a un refresh token, on laisse l'intercepteur tenter le refresh
    // Sinon, on redirige vers login
    if (this.auth.hasRefreshToken()) {
      // L'intercepteur gérera le refresh au prochain appel API
      // On vérifie quand même si le token est vraiment expiré
      const token = localStorage.getItem('accessToken');
      if (token) {
        // Token existe mais est possiblement expiré, laisser passer
        // L'intercepteur s'occupera du refresh si nécessaire
        return true;
      }
    }

    // Pas de token valide, rediriger vers login avec l'URL de retour
    console.log('AuthGuard: User not authenticated, redirecting to login');
    this.router.navigate(['/login'], {
      queryParams: {
        returnUrl: state.url,
        expired: 'true'
      }
    });
    return false;
  }
}
