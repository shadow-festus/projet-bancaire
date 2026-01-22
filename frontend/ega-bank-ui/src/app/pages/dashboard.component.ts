import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of, Subject, takeUntil, timeout } from 'rxjs';
import { AccountResponse } from '../models/account.model';
import { ClientResponse } from '../models/client.model';
import { PageResponse } from '../models/page.model';
import { AccountService } from '../services/account.service';
import { ClientService } from '../services/client.service';
import { DashboardService } from '../services/dashboard.service';
import { AppStore } from '../stores/app.store';
import { AuthService } from '../services/auth.service';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit, OnDestroy {
    isLoading = true;
    hasError = false;
    errorMessage = '';
    private cdr: ChangeDetectorRef;

    stats = {
        totalClients: 0,
        totalAccounts: 0,
        totalBalance: 0,
        activeAccounts: 0,
        totalTransactions: 0
    };
    recentClients: ClientResponse[] = [];
    recentAccounts: AccountResponse[] = [];

    isAdmin = false;
    clientId: number | null = null;

    private destroy$ = new Subject<void>();

    constructor(
        private clientService: ClientService,
        private accountService: AccountService,
        private dashboardService: DashboardService,
        private store: AppStore,
        cdr: ChangeDetectorRef,
        private authService: AuthService
    ) {
        this.cdr = cdr;
    }

    ngOnInit() {
        this.isAdmin = this.authService.getUserRole() === 'ROLE_ADMIN';
        this.clientId = this.authService.getClientId();

        this.loadData();

        // S'abonner aux changements du store pour rafraîchir automatiquement
        this.store.dataChanged$.pipe(
            takeUntil(this.destroy$)
        ).subscribe(event => {
            console.log('[Dashboard] Data change event received:', event);
            // Rafraîchir les données quand il y a un changement
            if (event.type === 'system' && event.action === 'refresh') {
                this.loadData();
            } else if (event.type === 'transaction' && event.action === 'balance_update') {
                // Mettre à jour les soldes localement si possible
                this.updateLocalBalances(event.data);
            } else if (event.type === 'account' || event.type === 'client') {
                // Rafraîchir pour les créations/suppressions
                this.loadData();
            }
        });
    }

    ngOnDestroy() {
        this.destroy$.next();
        this.destroy$.complete();
    }

    retry() {
        this.loadData();
    }

    private updateLocalBalances(data: { numeroCompte: string, newBalance: number }) {
        // Mettre à jour le compte dans la liste récente
        this.recentAccounts = this.recentAccounts.map(acc =>
            acc.numeroCompte === data.numeroCompte
                ? { ...acc, solde: data.newBalance }
                : acc
        );

        // Recalculer le total balance approximatif
        // (Pour une vraie mise à jour, on recharge depuis le backend)
        this.loadData();
    }

    private loadData() {
        this.isLoading = true;
        this.hasError = false;
        this.errorMessage = '';

        console.log('[Dashboard] Starting data load...');

        const requests: any = {
            dashboardStats: this.dashboardService.getStats().pipe(
                timeout(10000),
                catchError(err => {
                    console.error('[Dashboard] Error loading stats:', err);
                    return of(null);
                })
            ),
            recentAccounts: this.accountService.getAll(0, 5).pipe(
                timeout(10000),
                catchError(err => {
                    console.error('[Dashboard] Error loading recentAccounts:', err);
                    return of({ content: [], totalElements: 0, pageNumber: 0, pageSize: 5, totalPages: 0, first: true, last: true } as PageResponse<AccountResponse>);
                })
            )
        };

        if (this.isAdmin) {
            requests.recentClients = this.clientService.getAll(0, 5).pipe(
                timeout(10000),
                catchError(err => {
                    console.error('[Dashboard] Error loading recentClients:', err);
                    return of({ content: [], totalElements: 0, pageNumber: 0, pageSize: 5, totalPages: 0, first: true, last: true } as PageResponse<ClientResponse>);
                })
            );
        } else {
            requests.recentClients = of(null);
        }

        forkJoin(requests).subscribe({
            next: (results: any) => {
                console.log('[Dashboard] Received results:', results);

                const { dashboardStats, recentClients, recentAccounts } = results;

                // Stats
                if (dashboardStats) {
                    this.stats = dashboardStats;
                }

                // Lists
                this.recentClients = recentClients?.content || [];
                this.recentAccounts = recentAccounts?.content || [];

                this.isLoading = false;
                this.cdr.detectChanges();
            },
            error: (err: any) => {
                console.error('[Dashboard] Fatal error loading data:', err);
                this.isLoading = false;
                this.hasError = true;
                this.errorMessage = 'Échec du chargement des données.';
                this.cdr.detectChanges();
            }
        });
    }
}
