import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subject } from 'rxjs';
import { AccountResponse } from '../models/account.model';
import { TransactionResponse } from '../models/transaction.model';
import { AccountService } from '../services/account.service';
import { TransactionService } from '../services/transaction.service';
import { AppStore } from '../stores/app.store';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-transaction-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="tx-container">
      <div class="tx-layout">
        <!-- Left Panel - Transaction Type -->
        <div class="tx-sidebar animate-slide-in">
          <a [routerLink]="returnAccountId ? '/transactions' : '/accounts'" 
             [queryParams]="returnAccountId ? { accountId: returnAccountId } : {}"
             class="back-btn">
            <i class="ri-arrow-left-line"></i>
          </a>
          
          <div class="sidebar-header">
            <h1 class="sidebar-title">Nouvelle Transaction</h1>
            <p class="sidebar-subtitle">Sélectionnez le type d'opération</p>
          </div>

          <div class="tx-type-list">
            <label class="tx-type-option" [class.active]="selectedType === 'DEPOT'" (click)="selectType('DEPOT')">
              <input type="radio" name="txType" [checked]="selectedType === 'DEPOT'">
              <div class="tx-type-icon deposit">
                <i class="ri-add-line"></i>
              </div>
              <div class="tx-type-info">
                <span class="tx-type-name">Dépôt</span>
                <span class="tx-type-desc">Alimenter un compte</span>
              </div>
              <i class="ri-arrow-right-s-line tx-type-arrow"></i>
            </label>

            <label class="tx-type-option" [class.active]="selectedType === 'RETRAIT'" (click)="selectType('RETRAIT')">
              <input type="radio" name="txType" [checked]="selectedType === 'RETRAIT'">
              <div class="tx-type-icon withdrawal">
                <i class="ri-subtract-line"></i>
              </div>
              <div class="tx-type-info">
                <span class="tx-type-name">Retrait</span>
                <span class="tx-type-desc">Retirer des fonds</span>
              </div>
              <i class="ri-arrow-right-s-line tx-type-arrow"></i>
            </label>

            <label class="tx-type-option" [class.active]="selectedType === 'VIREMENT'" (click)="selectType('VIREMENT')">
              <input type="radio" name="txType" [checked]="selectedType === 'VIREMENT'">
              <div class="tx-type-icon transfer">
                <i class="ri-arrow-left-right-line"></i>
              </div>
              <div class="tx-type-info">
                <span class="tx-type-name">Virement</span>
                <span class="tx-type-desc">Transférer entre comptes</span>
              </div>
              <i class="ri-arrow-right-s-line tx-type-arrow"></i>
            </label>
          </div>
        </div>

        <!-- Right Panel - Form -->
        <div class="tx-main animate-slide-in" style="animation-delay: 100ms">
          <!-- Alerts -->
          <div *ngIf="errorMessage" class="alert alert-danger">
            <i class="ri-error-warning-line"></i>
            <span>{{ errorMessage }}</span>
          </div>

          <div *ngIf="successMessage" class="alert alert-success">
            <i class="ri-checkbox-circle-line"></i>
            <span>{{ successMessage }}</span>
          </div>

          <form [formGroup]="form" (ngSubmit)="submit()">
            <!-- Account Selection -->
            <div class="form-section">
              <h2 class="section-title">
                <i class="ri-wallet-3-line"></i>
                {{ selectedType === 'VIREMENT' ? 'Compte Source' : 'Compte' }}
              </h2>

              <div *ngIf="isLoadingAccounts" class="skeleton-select"></div>

              <div *ngIf="!isLoadingAccounts && accounts.length === 0" class="empty-accounts">
                <i class="ri-bank-card-2-line"></i>
                <p>Aucun compte disponible</p>
                <a routerLink="/accounts/new" class="btn btn-primary btn-sm">
                  <i class="ri-add-line"></i> Créer un compte
                </a>
              </div>

              <div *ngIf="!isLoadingAccounts && accounts.length > 0" class="account-select-wrapper">
                <select formControlName="accountNumber" class="account-select">
                  <option value="">Sélectionnez un compte...</option>
                  <option *ngFor="let account of accounts" [value]="account.numeroCompte">
                    {{ account.numeroCompte }} • {{ getTypeDisplay(account.typeCompte) }} • {{ account.clientNomComplet }}
                  </option>
                </select>
                <i class="ri-arrow-down-s-line select-arrow"></i>
              </div>

              <!-- Source Account Card -->
              <div *ngIf="sourceAccount" class="account-card animate-fade-in">
                <div class="account-card-header">
                  <div class="account-avatar">
                    {{ sourceAccount.clientNomComplet?.charAt(0) || 'C' }}
                  </div>
                  <div class="account-info">
                    <span class="account-holder">{{ sourceAccount.clientNomComplet }}</span>
                    <span class="account-number">{{ sourceAccount.numeroCompte }}</span>
                  </div>
                  <span class="account-type-badge">{{ getTypeDisplay(sourceAccount.typeCompte) }}</span>
                </div>
                <div class="account-card-balance">
                  <span class="balance-label">Solde disponible</span>
                  <span class="balance-amount" [class.positive]="sourceAccount.solde > 0" [class.negative]="sourceAccount.solde <= 0">
                    {{ sourceAccount.solde | number:'1.0-0' }} FCFA
                  </span>
                </div>
              </div>
            </div>

            <!-- Target Account (Transfer only) -->
            <div *ngIf="selectedType === 'VIREMENT'" class="form-section animate-fade-in">
              <h2 class="section-title">
                <i class="ri-arrow-right-line"></i>
                Compte Destinataire
              </h2>

              <div class="account-select-wrapper">
                <select formControlName="targetAccountNumber" class="account-select">
                  <option value="">Sélectionnez le destinataire...</option>
                  <option *ngFor="let account of getTargetAccounts()" [value]="account.numeroCompte">
                    {{ account.numeroCompte }} • {{ getTypeDisplay(account.typeCompte) }} • {{ account.clientNomComplet }}
                  </option>
                </select>
                <i class="ri-arrow-down-s-line select-arrow"></i>
              </div>
            </div>

            <!-- Amount Section -->
            <div class="form-section">
              <h2 class="section-title">
                <i class="ri-money-dollar-circle-line"></i>
                Montant
              </h2>

              <div class="amount-input-wrapper">
                <input 
                  type="number" 
                  formControlName="amount"
                  class="amount-input"
                  placeholder="0"
                  min="1"
                  step="1">
                <span class="amount-currency">FCFA</span>
              </div>

              <!-- Balance Feedback -->
              <div *ngIf="form.get('amount')?.value > 0 && sourceAccount" class="balance-feedback animate-fade-in">
                <ng-container *ngIf="selectedType !== 'DEPOT'">
                  <div class="feedback-item" [class.success]="isBalanceSufficient()" [class.error]="!isBalanceSufficient()">
                    <i [class]="isBalanceSufficient() ? 'ri-checkbox-circle-fill' : 'ri-close-circle-fill'"></i>
                    <span>{{ isBalanceSufficient() ? 'Solde suffisant' : 'Solde insuffisant' }}</span>
                  </div>
                </ng-container>
                <div *ngIf="selectedType === 'DEPOT'" class="feedback-item success">
                  <i class="ri-arrow-up-circle-fill"></i>
                  <span>Nouveau solde : {{ (sourceAccount.solde + form.get('amount')?.value) | number:'1.0-0' }} FCFA</span>
                </div>
              </div>
            </div>

            <!-- Description Section -->
            <div class="form-section">
              <h2 class="section-title">
                <i class="ri-file-text-line"></i>
                Description <span class="optional">(optionnel)</span>
              </h2>
              <input 
                type="text" 
                formControlName="description"
                class="description-input"
                placeholder="Ex: Paiement loyer mensuel">
            </div>

            <!-- Submit Button -->
            <div class="form-actions">
              <button 
                type="submit" 
                [disabled]="form.invalid || isSubmitting || accounts.length === 0 || !isBalanceSufficient()"
                class="submit-btn"
                [class.deposit]="selectedType === 'DEPOT'"
                [class.withdrawal]="selectedType === 'RETRAIT'"
                [class.transfer]="selectedType === 'VIREMENT'">
                <span *ngIf="isSubmitting" class="btn-content">
                  <span class="spinner"></span>
                  Traitement en cours...
                </span>
                <span *ngIf="!isSubmitting" class="btn-content">
                  <i [class]="getSubmitIcon()"></i>
                  {{ getSubmitLabel() }}
                </span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Payment Processor Simulation Modal -->
    <div *ngIf="showPaymentProcessor" class="payment-overlay">
      <div class="payment-modal">
        <div class="payment-header">
          <div class="payment-logo">
            <i class="ri-bank-card-line"></i>
          </div>
          <h2>{{ selectedType === 'DEPOT' ? 'Dépôt en cours' : 'Retrait en cours' }}</h2>
        </div>

        <div class="payment-amount">
          {{ form.get('amount')?.value | number:'1.0-0' }} <span class="currency">FCFA</span>
        </div>

        <div class="payment-steps">
          <div class="step" [class.active]="processingStep >= 1" [class.done]="processingStep > 1">
            <div class="step-icon">
              <i *ngIf="processingStep <= 1" class="ri-shield-check-line"></i>
              <i *ngIf="processingStep > 1" class="ri-check-line"></i>
            </div>
            <span>Validation</span>
          </div>
          
          <div class="step-connector" [class.active]="processingStep >= 2"></div>
          
          <div class="step" [class.active]="processingStep >= 2" [class.done]="processingStep > 2">
            <div class="step-icon">
              <i *ngIf="processingStep <= 2" class="ri-bank-card-line"></i>
              <i *ngIf="processingStep > 2" class="ri-check-line"></i>
            </div>
            <span>Traitement</span>
          </div>
          
          <div class="step-connector" [class.active]="processingStep >= 3"></div>
          
          <div class="step" [class.active]="processingStep >= 3" [class.done]="processingStep > 3">
            <div class="step-icon">
              <i *ngIf="processingStep <= 3" class="ri-secure-payment-line"></i>
              <i *ngIf="processingStep > 3" class="ri-check-line"></i>
            </div>
            <span>Finalisation</span>
          </div>
        </div>

        <div class="payment-status" [class.success]="processingStep === 4">
          <div class="status-icon" [class.spinning]="processingStep < 4">
            <i [class]="processingStep === 4 ? 'ri-checkbox-circle-fill' : processingIcon"></i>
          </div>
          <p>{{ processingMessage }}</p>
        </div>

        <div *ngIf="processingStep === 4" class="payment-success-badge">
          <i class="ri-check-double-line"></i>
          Opération réussie
        </div>
      </div>
    </div>
  `,
  styles: [`
    .tx-container {
      min-height: calc(100vh - 70px);
      padding: 1.5rem;
      background: #f8fafc;
    }

    .tx-layout {
      display: grid;
      grid-template-columns: 320px 1fr;
      gap: 1.5rem;
      max-width: 1000px;
      margin: 0 auto;
    }

    /* ===== SIDEBAR ===== */
    .tx-sidebar {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 1rem;
      padding: 1.5rem;
      height: fit-content;
      position: sticky;
      top: 1.5rem;
    }

    .back-btn {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #64748b;
      font-size: 1.25rem;
      transition: all 0.15s;
      margin-bottom: 1.5rem;
    }

    .back-btn:hover {
      background: #f1f5f9;
      color: #1e293b;
    }

    .sidebar-header {
      margin-bottom: 1.5rem;
    }

    .sidebar-title {
      font-size: 1.375rem;
      font-weight: 700;
      color: #0f172a;
      margin-bottom: 0.25rem;
    }

    .sidebar-subtitle {
      font-size: 0.875rem;
      color: #64748b;
    }

    .tx-type-list {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .tx-type-option {
      display: flex;
      align-items: center;
      gap: 0.875rem;
      padding: 1rem;
      border: 2px solid #e2e8f0;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.2s;
      position: relative;
    }

    .tx-type-option input {
      position: absolute;
      opacity: 0;
    }

    .tx-type-option:hover {
      border-color: #cbd5e1;
      background: #fafafa;
    }

    .tx-type-option.active {
      border-color: #3b82f6;
      background: rgba(59, 130, 246, 0.05);
    }

    .tx-type-icon {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
      flex-shrink: 0;
    }

    .tx-type-icon.deposit {
      background: rgba(16, 185, 129, 0.1);
      color: #10b981;
    }

    .tx-type-icon.withdrawal {
      background: rgba(239, 68, 68, 0.1);
      color: #ef4444;
    }

    .tx-type-icon.transfer {
      background: rgba(59, 130, 246, 0.1);
      color: #3b82f6;
    }

    .tx-type-info {
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    .tx-type-name {
      font-weight: 600;
      color: #1e293b;
      font-size: 0.9375rem;
    }

    .tx-type-desc {
      font-size: 0.75rem;
      color: #64748b;
      margin-top: 0.125rem;
    }

    .tx-type-arrow {
      color: #cbd5e1;
      font-size: 1.25rem;
      transition: all 0.2s;
    }

    .tx-type-option.active .tx-type-arrow {
      color: #3b82f6;
      transform: translateX(4px);
    }

    /* ===== MAIN PANEL ===== */
    .tx-main {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 1rem;
      padding: 2rem;
    }

    .alert {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1rem;
      border-radius: 12px;
      margin-bottom: 1.5rem;
      font-size: 0.875rem;
    }

    .alert i {
      font-size: 1.25rem;
    }

    .alert-danger {
      background: #fef2f2;
      border: 1px solid #fee2e2;
      color: #dc2626;
    }

    .alert-success {
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
      color: #15803d;
    }

    /* Form Sections */
    .form-section {
      margin-bottom: 2rem;
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.9375rem;
      font-weight: 600;
      color: #334155;
      margin-bottom: 1rem;
    }

    .section-title i {
      color: #3b82f6;
    }

    .optional {
      font-weight: 400;
      color: #94a3b8;
      font-size: 0.8125rem;
    }

    /* Account Select */
    .account-select-wrapper {
      position: relative;
    }

    .account-select {
      width: 100%;
      padding: 0.875rem 2.5rem 0.875rem 1rem;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      font-size: 0.9375rem;
      color: #1e293b;
      background: white;
      cursor: pointer;
      appearance: none;
      transition: all 0.15s;
    }

    .account-select:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
    }

    .select-arrow {
      position: absolute;
      right: 1rem;
      top: 50%;
      transform: translateY(-50%);
      color: #94a3b8;
      pointer-events: none;
      font-size: 1.25rem;
    }

    .skeleton-select {
      height: 52px;
      border-radius: 12px;
      background: linear-gradient(110deg, #f1f5f9 8%, #f8fafc 18%, #f1f5f9 33%);
      background-size: 200% 100%;
      animation: shimmer 1.5s linear infinite;
    }

    @keyframes shimmer {
      from { background-position: 200% 0; }
      to { background-position: -200% 0; }
    }

    /* Empty Accounts */
    .empty-accounts {
      text-align: center;
      padding: 2rem;
      background: #f8fafc;
      border: 2px dashed #e2e8f0;
      border-radius: 12px;
    }

    .empty-accounts i {
      font-size: 2.5rem;
      color: #cbd5e1;
      margin-bottom: 0.75rem;
    }

    .empty-accounts p {
      color: #64748b;
      margin-bottom: 1rem;
    }

    /* Account Card */
    .account-card {
      margin-top: 1rem;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      overflow: hidden;
    }

    .account-card-header {
      display: flex;
      align-items: center;
      gap: 0.875rem;
      padding: 1rem;
      background: #f8fafc;
    }

    .account-avatar {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 0.9375rem;
    }

    .account-info {
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    .account-holder {
      font-weight: 600;
      color: #1e293b;
      font-size: 0.9375rem;
    }

    .account-number {
      font-size: 0.75rem;
      color: #64748b;
      font-family: 'SF Mono', Monaco, monospace;
    }

    .account-type-badge {
      padding: 0.25rem 0.625rem;
      border-radius: 20px;
      font-size: 0.6875rem;
      font-weight: 600;
      text-transform: uppercase;
      background: rgba(59, 130, 246, 0.1);
      color: #3b82f6;
    }

    .account-card-balance {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem;
    }

    .balance-label {
      font-size: 0.8125rem;
      color: #64748b;
    }

    .balance-amount {
      font-size: 1.25rem;
      font-weight: 700;
      font-family: 'SF Mono', Monaco, monospace;
    }

    .balance-amount.positive {
      color: #10b981;
    }

    .balance-amount.negative {
      color: #ef4444;
    }

    /* Amount Input */
    .amount-input-wrapper {
      position: relative;
    }

    .amount-input {
      width: 100%;
      padding: 1.25rem 5rem 1.25rem 1.25rem;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      font-size: 2rem;
      font-weight: 700;
      font-family: 'SF Mono', Monaco, monospace;
      color: #1e293b;
      text-align: right;
      transition: all 0.15s;
    }

    .amount-input::placeholder {
      color: #cbd5e1;
    }

    .amount-input:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
    }

    .amount-currency {
      position: absolute;
      right: 1.25rem;
      top: 50%;
      transform: translateY(-50%);
      font-size: 0.875rem;
      font-weight: 600;
      color: #94a3b8;
      background: #f1f5f9;
      padding: 0.375rem 0.75rem;
      border-radius: 6px;
    }

    /* Balance Feedback */
    .balance-feedback {
      margin-top: 0.75rem;
    }

    .feedback-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.875rem;
      font-weight: 500;
    }

    .feedback-item.success {
      color: #10b981;
    }

    .feedback-item.error {
      color: #ef4444;
    }

    /* Description Input */
    .description-input {
      width: 100%;
      padding: 0.875rem 1rem;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      font-size: 0.9375rem;
      color: #1e293b;
      transition: all 0.15s;
    }

    .description-input::placeholder {
      color: #94a3b8;
    }

    .description-input:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
    }

    /* Submit Button */
    .form-actions {
      margin-top: 2rem;
      padding-top: 1.5rem;
      border-top: 1px solid #f1f5f9;
    }

    .submit-btn {
      width: 100%;
      padding: 1rem 1.5rem;
      border: none;
      border-radius: 12px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }

    .submit-btn .btn-content {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.625rem;
    }

    .submit-btn.deposit {
      background: linear-gradient(135deg, #10b981, #059669);
      color: white;
      box-shadow: 0 4px 14px rgba(16, 185, 129, 0.4);
    }

    .submit-btn.deposit:hover:not(:disabled) {
      box-shadow: 0 6px 20px rgba(16, 185, 129, 0.5);
      transform: translateY(-2px);
    }

    .submit-btn.withdrawal {
      background: linear-gradient(135deg, #ef4444, #dc2626);
      color: white;
      box-shadow: 0 4px 14px rgba(239, 68, 68, 0.4);
    }

    .submit-btn.withdrawal:hover:not(:disabled) {
      box-shadow: 0 6px 20px rgba(239, 68, 68, 0.5);
      transform: translateY(-2px);
    }

    .submit-btn.transfer {
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      color: white;
      box-shadow: 0 4px 14px rgba(59, 130, 246, 0.4);
    }

    .submit-btn.transfer:hover:not(:disabled) {
      box-shadow: 0 6px 20px rgba(59, 130, 246, 0.5);
      transform: translateY(-2px);
    }

    .submit-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
      transform: none !important;
      box-shadow: none !important;
    }

    .spinner {
      width: 1.25rem;
      height: 1.25rem;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    /* Animations */
    .animate-slide-in {
      animation: slideIn 0.4s ease-out;
    }

    .animate-fade-in {
      animation: fadeIn 0.3s ease-out;
    }

    @keyframes slideIn {
      from { opacity: 0; transform: translateY(12px); }
      to { opacity: 1; transform: translateY(0); }
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    /* Button styles */
    .btn {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 8px;
      font-size: 0.875rem;
      font-weight: 600;
      cursor: pointer;
      text-decoration: none;
      transition: all 0.15s;
    }

    .btn-primary {
      background: #3b82f6;
      color: white;
    }

    .btn-sm {
      padding: 0.5rem 0.875rem;
      font-size: 0.8125rem;
    }

    /* ===== RESPONSIVE ===== */
    @media (max-width: 900px) {
      .tx-layout {
        grid-template-columns: 1fr;
      }

      .tx-sidebar {
        position: static;
      }

      .tx-type-list {
        flex-direction: row;
        overflow-x: auto;
        gap: 0.5rem;
        padding-bottom: 0.5rem;
      }

      .tx-type-option {
        flex-shrink: 0;
        min-width: 140px;
        flex-direction: column;
        text-align: center;
        padding: 1rem 0.75rem;
      }

      .tx-type-arrow {
        display: none;
      }
    }

    @media (max-width: 480px) {
      .tx-container {
        padding: 1rem;
      }

      .tx-main {
        padding: 1.5rem;
      }

      .amount-input {
        font-size: 1.5rem;
        padding: 1rem 4rem 1rem 1rem;
      }
    }

    /* ===== PAYMENT PROCESSOR MODAL ===== */
    .payment-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(15, 23, 42, 0.8);
      backdrop-filter: blur(8px);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      animation: fadeIn 0.3s ease-out;
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    .payment-modal {
      background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
      border-radius: 24px;
      padding: 2.5rem;
      width: 100%;
      max-width: 420px;
      text-align: center;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
      animation: slideUp 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      border: 1px solid rgba(255, 255, 255, 0.1);
    }

    @keyframes slideUp {
      from {
        opacity: 0;
        transform: translateY(30px) scale(0.95);
      }
      to {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
    }

    .payment-header {
      margin-bottom: 1.5rem;
    }

    .payment-logo {
      width: 64px;
      height: 64px;
      border-radius: 16px;
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 1rem;
      font-size: 1.75rem;
      color: white;
      box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4);
    }

    .payment-header h2 {
      color: white;
      font-size: 1.25rem;
      font-weight: 600;
      margin: 0;
    }

    .payment-amount {
      font-size: 2.5rem;
      font-weight: 700;
      color: white;
      margin-bottom: 2rem;
      letter-spacing: -0.02em;
    }

    .payment-amount .currency {
      font-size: 1.25rem;
      color: rgba(255, 255, 255, 0.6);
      font-weight: 500;
    }

    .payment-steps {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      margin-bottom: 2rem;
    }

    .payment-steps .step {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
      opacity: 0.4;
      transition: all 0.3s;
    }

    .payment-steps .step.active {
      opacity: 1;
    }

    .payment-steps .step.done .step-icon {
      background: #10b981;
    }

    .payment-steps .step-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 1.125rem;
      transition: background 0.3s;
    }

    .payment-steps .step.active .step-icon {
      background: #3b82f6;
    }

    .payment-steps .step span {
      font-size: 0.75rem;
      color: rgba(255, 255, 255, 0.7);
      font-weight: 500;
    }

    .payment-steps .step-connector {
      width: 40px;
      height: 2px;
      background: rgba(255, 255, 255, 0.2);
      margin-bottom: 1.5rem;
      transition: background 0.3s;
    }

    .payment-steps .step-connector.active {
      background: #10b981;
    }

    .payment-status {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.75rem;
    }

    .payment-status .status-icon {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #3b82f6;
      font-size: 1.5rem;
    }

    .payment-status .status-icon.spinning i {
      animation: spin 1s linear infinite;
    }

    .payment-status.success .status-icon {
      background: rgba(16, 185, 129, 0.2);
      color: #10b981;
    }

    .payment-status p {
      color: rgba(255, 255, 255, 0.8);
      font-size: 0.9375rem;
      margin: 0;
    }

    .payment-success-badge {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      background: rgba(16, 185, 129, 0.2);
      color: #10b981;
      padding: 0.75rem 1.5rem;
      border-radius: 100px;
      font-size: 0.875rem;
      font-weight: 600;
      margin-top: 1.5rem;
      animation: bounceIn 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    }

    @keyframes bounceIn {
      0% {
        opacity: 0;
        transform: scale(0.3);
      }
      50% {
        opacity: 1;
        transform: scale(1.1);
      }
      100% {
        transform: scale(1);
      }
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class TransactionFormComponent implements OnInit, OnDestroy {
  form: FormGroup;
  accounts: AccountResponse[] = [];
  sourceAccount: AccountResponse | null = null;
  targetAccount: AccountResponse | null = null;
  isLoadingAccounts = true;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  returnAccountId: string | null = null;
  selectedType: 'DEPOT' | 'RETRAIT' | 'VIREMENT' = 'DEPOT';

  // Payment processor simulation
  showPaymentProcessor = false;
  processingStep = 0; // 0: idle, 1: validating, 2: processing, 3: finalizing, 4: done
  processingMessage = '';
  processingIcon = '';

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private txService: TransactionService,
    private accountService: AccountService,
    private router: Router,
    private route: ActivatedRoute,
    private store: AppStore,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {
    this.form = this.fb.group({
      type: ['DEPOT', Validators.required],
      accountNumber: ['', Validators.required],
      targetAccountNumber: [''],
      amount: [null, [Validators.required, Validators.min(1)]],
      description: [''],
    });

    this.form.get('accountNumber')?.valueChanges.subscribe(accountNumber => {
      this.sourceAccount = this.accounts.find(a => a.numeroCompte === accountNumber) || null;
    });

    this.form.get('targetAccountNumber')?.valueChanges.subscribe(accountNumber => {
      this.targetAccount = this.accounts.find(a => a.numeroCompte === accountNumber) || null;
    });

    this.form.get('type')?.valueChanges.subscribe(type => {
      if (type !== 'VIREMENT') {
        this.form.patchValue({ targetAccountNumber: '' });
        this.targetAccount = null;
      }
    });
  }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      this.returnAccountId = params.get('accountId');
      if (this.returnAccountId) {
        this.form.patchValue({ accountNumber: this.returnAccountId });
      }
    });
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.isLoadingAccounts = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    const role = this.authService.getUserRole();
    const isAdmin = role === 'ROLE_ADMIN';
    const clientId = this.authService.getClientId();

    if (!isAdmin && clientId) {
      // Mode Client
      this.accountService.getByClient(clientId).subscribe({
        next: (accounts) => {
          this.accounts = (accounts || []).filter(a => a.actif);
          this.isLoadingAccounts = false;
          this.postLoadAccounts();
        },
        error: (err) => this.handleLoadError(err)
      });
    } else {
      // Mode Admin
      this.accountService.getAll(0, 200).subscribe({
        next: (response) => {
          this.accounts = (response.content || []).filter(a => a.actif);
          this.isLoadingAccounts = false;
          this.postLoadAccounts();
        },
        error: (err) => this.handleLoadError(err)
      });
    }
  }

  private postLoadAccounts() {
    if (this.returnAccountId) {
      this.sourceAccount = this.accounts.find(a => a.numeroCompte === this.returnAccountId) || null;
    }
    this.cdr.detectChanges();
  }

  private handleLoadError(err: any) {
    console.error('Failed to load accounts', err);
    this.errorMessage = 'Échec du chargement des comptes. Veuillez réessayer.';
    this.isLoadingAccounts = false;
    this.cdr.detectChanges();
  }

  getTargetAccounts(): AccountResponse[] {
    const sourceNum = this.form.value.accountNumber;
    return this.accounts.filter(a => a.numeroCompte !== sourceNum);
  }

  selectType(type: 'DEPOT' | 'RETRAIT' | 'VIREMENT'): void {
    this.selectedType = type;
    this.form.patchValue({ type });
    if (type !== 'VIREMENT') {
      this.form.patchValue({ targetAccountNumber: '' });
      this.targetAccount = null;
    }
  }

  getTypeDisplay(typeCompte: string): string {
    const types: Record<string, string> = {
      EPARGNE: 'Épargne',
      COURANT: 'Courant',
    };
    return types[typeCompte] || typeCompte;
  }

  isBalanceSufficient(): boolean {
    if (this.selectedType === 'DEPOT') return true;
    if (!this.sourceAccount) return false;
    return this.sourceAccount.solde >= (this.form.value.amount || 0);
  }

  getSubmitIcon(): string {
    switch (this.selectedType) {
      case 'DEPOT': return 'ri-add-circle-line';
      case 'RETRAIT': return 'ri-subtract-line';
      case 'VIREMENT': return 'ri-arrow-left-right-line';
      default: return 'ri-check-line';
    }
  }

  getSubmitLabel(): string {
    switch (this.selectedType) {
      case 'DEPOT': return 'Effectuer le Dépôt';
      case 'RETRAIT': return 'Effectuer le Retrait';
      case 'VIREMENT': return 'Effectuer le Virement';
      default: return 'Valider';
    }
  }

  submit(): void {
    if (this.form.invalid) return;

    const v = this.form.value;

    if (v.type === 'VIREMENT' && !v.targetAccountNumber) {
      this.errorMessage = 'Veuillez sélectionner un compte destinataire pour le virement.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    // Pour les dépôts et retraits, afficher la simulation du processeur de paiement
    if (v.type === 'DEPOT' || v.type === 'RETRAIT') {
      this.startPaymentSimulation();
    } else {
      // Virement direct sans simulation
      this.isSubmitting = true;
      this.executeTransaction();
    }
  }

  private startPaymentSimulation(): void {
    this.showPaymentProcessor = true;
    this.processingStep = 1;
    this.processingMessage = 'Vérification des informations...';
    this.processingIcon = 'ri-shield-check-line';
    this.cdr.detectChanges();

    // Étape 1: Validation (1.2s)
    setTimeout(() => {
      this.processingStep = 2;
      this.processingMessage = 'Traitement bancaire en cours...';
      this.processingIcon = 'ri-bank-card-line';
      this.cdr.detectChanges();

      // Étape 2: Traitement (1.5s)
      setTimeout(() => {
        this.processingStep = 3;
        this.processingMessage = 'Finalisation de l\'opération...';
        this.processingIcon = 'ri-secure-payment-line';
        this.cdr.detectChanges();

        // Étape 3: Finalisation (0.8s)
        setTimeout(() => {
          this.isSubmitting = true;
          this.executeTransaction();
        }, 800);
      }, 1500);
    }, 1200);
  }

  private executeTransaction(): void {
    const v = this.form.value;

    if (v.type === 'VIREMENT') {
      this.txService.transfer({
        compteSource: String(v.accountNumber),
        compteDestination: String(v.targetAccountNumber),
        montant: Number(v.amount),
        description: v.description || undefined
      }).subscribe({
        next: (tx) => this.handleSuccess('Virement effectué avec succès !', tx),
        error: (e) => this.handleError(e)
      });
    } else if (v.type === 'DEPOT') {
      this.txService.deposit(String(v.accountNumber), {
        montant: Number(v.amount),
        description: v.description || undefined
      }).subscribe({
        next: (tx) => this.handleSuccess('Dépôt effectué avec succès !', tx),
        error: (e) => this.handleError(e)
      });
    } else {
      this.txService.withdraw(String(v.accountNumber), {
        montant: Number(v.amount),
        description: v.description || undefined
      }).subscribe({
        next: (tx) => this.handleSuccess('Retrait effectué avec succès !', tx),
        error: (e) => this.handleError(e)
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private handleSuccess(message: string, transaction?: TransactionResponse): void {
    // Show success in payment processor if visible
    if (this.showPaymentProcessor) {
      this.processingStep = 4;
      this.processingMessage = 'Opération réussie !';
      this.processingIcon = 'ri-checkbox-circle-fill';
      this.cdr.detectChanges();
    }

    this.successMessage = message;
    this.isSubmitting = false;

    if (transaction && transaction.soldeApres !== undefined) {
      const accountNumber = this.form.value.accountNumber;
      this.store.updateAccountBalance(accountNumber, transaction.soldeApres);

      if (this.form.value.type === 'VIREMENT' && this.targetAccount) {
        this.store.triggerFullRefresh();
      }
    } else {
      this.store.triggerFullRefresh();
    }

    this.store.incrementTransactionCount();

    setTimeout(() => {
      this.showPaymentProcessor = false;
      if (this.returnAccountId) {
        this.router.navigate(['/transactions'], { queryParams: { accountId: this.returnAccountId } });
      } else {
        this.router.navigateByUrl('/accounts');
      }
    }, 1500);
  }

  private handleError(err: any): void {
    console.error('Transaction failed', err);
    this.showPaymentProcessor = false;
    this.isSubmitting = false;
    this.errorMessage = err.error?.message || 'Échec de la transaction. Veuillez réessayer.';
    this.cdr.detectChanges();
  }
}
