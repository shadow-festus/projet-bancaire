import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard pour protéger les routes réservées aux administrateurs.
 * Redirige vers la page d'accueil si l'utilisateur n'est pas admin.
 */
@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {
    constructor(private auth: AuthService, private router: Router) { }

    canActivate(): boolean {
        if (this.auth.getUserRole() === 'ROLE_ADMIN') {
            return true;
        }

        console.log('AdminGuard: User is not admin, redirecting to home');
        this.router.navigateByUrl('/');
        return false;
    }
}
