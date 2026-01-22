import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { AccountResponse } from '../models/account.model';
import { AuthService } from '../services/auth.service';
import { AccountService } from '../services/account.service';
import { ClientService } from '../services/client.service';
import { AppStore } from '../stores/app.store';

@Component({
  standalone: true,
  selector: 'app-accounts',
  imports: [CommonModule, RouterLink],
  templateUrl: './accounts.component.html',
})
export class AccountsComponent implements OnInit, OnDestroy {
  accounts: AccountResponse[] = [];
  clientId: number | null = null;
  isLoading = true;
  errorMessage = '';
  isAdmin = false;
  // Cache for client names
  private clientCache: Map<number, string> = new Map();
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private accountService: AccountService,
    private clientService: ClientService,
    private store: AppStore,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    const role = this.authService.getUserRole();
    this.isAdmin = role === 'ROLE_ADMIN';

    this.route.queryParamMap.subscribe((map) => {
      if (!this.isAdmin) {
        // Si c'est un client, on force son ID
        this.clientId = this.authService.getClientId();
      } else {
        // Si c'est un admin, on prend le paramètre d'URL s'il existe
        const clientIdParam = map.get('clientId');
        this.clientId = clientIdParam ? Number(clientIdParam) : null;
      }
      this.loadAccounts();
    });

    // S'abonner aux changements du store
    this.store.dataChanged$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(event => {
      console.log('[Accounts] Data change event:', event);
      if (event.type === 'account' || event.type === 'transaction' || event.type === 'system') {
        this.loadAccounts();
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadAccounts(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    if (this.clientId) {
      // Load accounts for specific client
      this.accountService.getByClient(this.clientId).subscribe({
        next: (accounts) => {
          this.accounts = accounts;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Failed to load accounts', err);
          this.errorMessage = 'Échec du chargement des comptes.';
          this.isLoading = false;
          this.cdr.detectChanges();
        },
      });
    } else {
      // Load all accounts
      this.accountService.getAll(0, 100).subscribe({
        next: (response) => {
          this.accounts = response.content || [];
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Failed to load accounts', err);
          this.errorMessage = 'Échec du chargement des comptes.';
          this.isLoading = false;
          this.cdr.detectChanges();
        },
      });
    }
  }

  getStatusClass(actif: boolean) {
    return actif ? 'badge-success' : 'badge-danger';
  }

  getStatusDisplay(actif: boolean) {
    return actif ? 'Actif' : 'Inactif';
  }

  getTypeDisplay(typeCompte: string) {
    const types: Record<string, string> = {
      EPARGNE: 'Épargne',
      COURANT: 'Courant',
    };
    return types[typeCompte] || typeCompte;
  }

  viewTransactions(numeroCompte: string) {
    this.router.navigate(['/transactions'], { queryParams: { accountId: numeroCompte } });
  }
}
