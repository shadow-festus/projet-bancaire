import { Routes } from '@angular/router';
import { AdminGuard } from './guards/admin.guard';
import { AuthGuard } from './guards/auth.guard';
import { AccountCreateComponent } from './pages/account-create.component';
import { AccountsComponent } from './pages/accounts.component';
import { ClientCreateComponent } from './pages/client-create.component';
import { ClientsComponent } from './pages/clients.component';
import { DashboardComponent } from './pages/dashboard.component';
import { LoginComponent } from './pages/login.component';
import { RegisterComponent } from './pages/register.component';
import { SettingsComponent } from './pages/settings.component';
import { TransactionFormComponent } from './pages/transaction-form.component';
import { TransactionsComponent } from './pages/transactions.component';

export const routes: Routes = [
	// Routes publiques (pas de guard)
	{ path: 'login', component: LoginComponent },
	{ path: 'register', component: RegisterComponent },

	// Routes protégées (nécessitent authentification)
	{ path: '', component: DashboardComponent, canActivate: [AuthGuard] },
	{ path: 'clients', component: ClientsComponent, canActivate: [AuthGuard, AdminGuard] },
	{ path: 'clients/new', component: ClientCreateComponent, canActivate: [AuthGuard, AdminGuard] },
	{ path: 'accounts', component: AccountsComponent, canActivate: [AuthGuard] },
	{ path: 'accounts/new', component: AccountCreateComponent, canActivate: [AuthGuard] },
	{ path: 'transactions', component: TransactionsComponent, canActivate: [AuthGuard] },
	{ path: 'transactions/new', component: TransactionFormComponent, canActivate: [AuthGuard] },
	{ path: 'settings', component: SettingsComponent, canActivate: [AuthGuard] },

	// Route wildcard - redirige vers la page d'accueil (qui redirigera vers login si non authentifié)
	{ path: '**', redirectTo: '' },
];
