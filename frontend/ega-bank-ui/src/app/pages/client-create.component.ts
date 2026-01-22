import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ClientService } from '../services/client.service';

@Component({
  selector: 'app-client-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="form-container">
      <div class="form-card animate-slide-in">
        <div class="form-header">
          <a routerLink="/clients" class="back-link">
            <i class="ri-arrow-left-line"></i>
          </a>
          <div>
            <h1 class="form-title">{{ isEditMode ? 'Modifier le Client' : 'Nouveau Client' }}</h1>
            <p class="form-subtitle">{{ isEditMode ? 'Modifiez les informations du client' : 'Remplissez les informations pour créer un nouveau client' }}</p>
          </div>
        </div>
        
        <!-- Error Message -->
        <div *ngIf="errorMessage" class="error-alert">
          <i class="ri-error-warning-line"></i>
          {{ errorMessage }}
        </div>
        
        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="form-grid">
            <div class="form-group">
              <label for="nom">Nom <span class="required">*</span></label>
              <input id="nom" formControlName="nom" placeholder="Entrez le nom" 
                [class.has-error]="form.get('nom')?.invalid && form.get('nom')?.touched" />
              <div *ngIf="form.get('nom')?.invalid && form.get('nom')?.touched" class="error-text">
                Le nom est requis
              </div>
            </div>
            
            <div class="form-group">
              <label for="prenom">Prénom <span class="required">*</span></label>
              <input id="prenom" formControlName="prenom" placeholder="Entrez le prénom"
                [class.has-error]="form.get('prenom')?.invalid && form.get('prenom')?.touched" />
              <div *ngIf="form.get('prenom')?.invalid && form.get('prenom')?.touched" class="error-text">
                Le prénom est requis
              </div>
            </div>
          </div>

          <div class="form-grid">
            <div class="form-group">
              <label for="dateNaissance">Date de naissance <span class="required">*</span></label>
              <input id="dateNaissance" type="date" formControlName="dateNaissance"
                [class.has-error]="form.get('dateNaissance')?.invalid && form.get('dateNaissance')?.touched" />
            </div>
            
            <div class="form-group">
              <label for="sexe">Sexe <span class="required">*</span></label>
              <select id="sexe" formControlName="sexe">
                <option value="MASCULIN">Masculin</option>
                <option value="FEMININ">Féminin</option>
              </select>
            </div>
          </div>

          <div class="form-grid">
            <div class="form-group">
              <label for="telephone">Téléphone</label>
              <div class="input-with-icon">
                <i class="ri-phone-line"></i>
                <input id="telephone" formControlName="telephone" placeholder="+228 90 00 00 00" />
              </div>
            </div>
            
            <div class="form-group">
              <label for="courriel">Email</label>
              <div class="input-with-icon">
                <i class="ri-mail-line"></i>
                <input id="courriel" type="email" formControlName="courriel" placeholder="exemple@email.com"
                  [class.has-error]="form.get('courriel')?.invalid && form.get('courriel')?.touched" />
              </div>
              <div *ngIf="form.get('courriel')?.invalid && form.get('courriel')?.touched" class="error-text">
                Email invalide
              </div>
            </div>
          </div>

          <div class="form-actions">
            <a routerLink="/clients" class="btn btn-secondary">
              <i class="ri-close-line"></i> Annuler
            </a>
            <button type="submit" [disabled]="form.invalid || isLoading" class="btn btn-primary">
              <span *ngIf="isLoading" class="spinner"></span>
              <i *ngIf="!isLoading" [class]="isEditMode ? 'ri-save-line' : 'ri-add-line'"></i>
              {{ isLoading ? 'Enregistrement...' : (isEditMode ? 'Enregistrer' : 'Créer le client') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .form-container {
      padding: 2rem;
      max-width: 700px;
      margin: 0 auto;
    }

    .form-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 1rem;
      overflow: hidden;
    }

    .form-header {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1.5rem;
      border-bottom: 1px solid #f1f5f9;
      background: #f8fafc;
    }

    .back-link {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      background: white;
      border: 1px solid #e2e8f0;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #64748b;
      font-size: 1.25rem;
      transition: all 0.15s;
    }

    .back-link:hover {
      background: #f1f5f9;
      color: #1e293b;
    }

    .form-title {
      font-size: 1.375rem;
      font-weight: 700;
      color: #0f172a;
      margin-bottom: 0.25rem;
    }

    .form-subtitle {
      font-size: 0.875rem;
      color: #64748b;
    }

    form {
      padding: 1.5rem;
    }

    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
      margin-bottom: 1rem;
    }

    .form-group {
      display: flex;
      flex-direction: column;
    }

    .form-group label {
      font-size: 0.875rem;
      font-weight: 600;
      color: #334155;
      margin-bottom: 0.5rem;
    }

    .required {
      color: #ef4444;
    }

    .form-group input,
    .form-group select {
      padding: 0.75rem 1rem;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      font-size: 0.9375rem;
      color: #1e293b;
      background: white;
      transition: all 0.15s;
    }

    .form-group input::placeholder {
      color: #94a3b8;
    }

    .form-group input:focus,
    .form-group select:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
    }

    .form-group input.has-error {
      border-color: #ef4444;
      background: #fef2f2;
    }

    .input-with-icon {
      position: relative;
    }

    .input-with-icon i {
      position: absolute;
      left: 1rem;
      top: 50%;
      transform: translateY(-50%);
      color: #94a3b8;
      font-size: 1.125rem;
    }

    .input-with-icon input {
      padding-left: 2.75rem;
      width: 100%;
    }

    .error-text {
      font-size: 0.75rem;
      color: #ef4444;
      margin-top: 0.375rem;
      font-weight: 500;
    }

    .form-actions {
      display: flex;
      gap: 1rem;
      justify-content: flex-end;
      padding-top: 1rem;
      border-top: 1px solid #f1f5f9;
      margin-top: 1rem;
    }

    .btn {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1.5rem;
      border-radius: 10px;
      font-size: 0.9375rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.15s;
      border: none;
      text-decoration: none;
    }

    .btn-primary {
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      color: white;
      box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
    }

    .btn-primary:hover:not(:disabled) {
      box-shadow: 0 4px 14px rgba(59, 130, 246, 0.4);
      transform: translateY(-1px);
    }

    .btn-primary:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .btn-secondary {
      background: white;
      color: #475569;
      border: 1px solid #e2e8f0;
    }

    .btn-secondary:hover {
      background: #f8fafc;
    }

    .spinner {
      width: 1rem;
      height: 1rem;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    @media (max-width: 640px) {
      .form-grid {
        grid-template-columns: 1fr;
      }

      .form-actions {
        flex-direction: column-reverse;
      }

      .form-actions .btn {
        width: 100%;
        justify-content: center;
      }
    }

    .error-alert {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 1rem 1.25rem;
      background: #fef2f2;
      border: 1px solid #fecaca;
      border-radius: 10px;
      color: #dc2626;
      font-size: 0.875rem;
      font-weight: 500;
      margin: 1rem 1.5rem 0;
    }

    .error-alert i {
      font-size: 1.125rem;
    }
  `]
})
export class ClientCreateComponent implements OnInit {
  form: any;
  isEditMode = false;
  clientId: number | null = null;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private clientService: ClientService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    // Default date of birth: 18 years ago (to pass @Past validation)
    const defaultDate = new Date();
    defaultDate.setFullYear(defaultDate.getFullYear() - 18);

    this.form = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      dateNaissance: [defaultDate.toISOString().split('T')[0], Validators.required],
      sexe: ['MASCULIN', Validators.required],
      telephone: [''],
      courriel: ['', Validators.email],
    });
  }

  ngOnInit() {
    this.route.queryParamMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.clientId = +id;
        this.loadClientData(this.clientId);
      }
    });
  }

  loadClientData(id: number) {
    this.isLoading = true;
    this.clientService.getById(id).subscribe({
      next: (client) => {
        this.form.patchValue({
          nom: client.nom,
          prenom: client.prenom,
          dateNaissance: client.dateNaissance,
          sexe: client.sexe,
          telephone: client.telephone,
          courriel: client.courriel
        });
        this.isLoading = false;
      },
      error: () => {
        alert('Échec du chargement des données du client');
        this.router.navigate(['/clients']);
      }
    });
  }

  submit() {
    if (this.form.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';

    // Clean the payload - remove spaces from phone number
    const payload = { ...this.form.value };
    if (payload.telephone) {
      payload.telephone = payload.telephone.replace(/\s/g, '');
    }
    // Remove empty optional fields to avoid validation errors
    if (!payload.telephone) delete payload.telephone;
    if (!payload.courriel) delete payload.courriel;

    console.log('Sending payload:', JSON.stringify(payload, null, 2));

    if (this.isEditMode && this.clientId) {
      this.clientService.update(this.clientId, payload).subscribe({
        next: () => this.router.navigateByUrl('/clients'),
        error: (err) => {
          console.error('Update failed', err);
          console.error('Error body:', err.error);
          this.errorMessage = this.extractErrorMessage(err);
          this.isLoading = false;
        }
      });
    } else {
      this.clientService.create(payload).subscribe({
        next: () => this.router.navigateByUrl('/clients'),
        error: (err) => {
          console.error('Create failed', err);
          console.error('Error body:', err.error);
          this.errorMessage = this.extractErrorMessage(err);
          this.isLoading = false;
        }
      });
    }
  }

  private extractErrorMessage(err: any): string {
    if (err.error) {
      // Check for validation errors array
      if (err.error.errors && Array.isArray(err.error.errors)) {
        return err.error.errors.join(', ');
      }
      // Check for message field
      if (err.error.message) {
        return err.error.message;
      }
      // Check if error is a string
      if (typeof err.error === 'string') {
        return err.error;
      }
    }
    return 'Échec de l\'opération. Vérifiez les informations saisies.';
  }
}
