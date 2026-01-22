import { AccountResponse } from './account.model';

// Sexe enum matching backend
export type Sexe = 'MASCULIN' | 'FEMININ';

export interface ClientResponse {
  id: number;
  nom: string;
  prenom: string;
  nomComplet?: string;
  dateNaissance?: string; // ISO date
  sexe?: Sexe;
  adresse?: string;
  telephone?: string;
  courriel?: string;
  nationalite?: string;
  createdAt?: string;
  nombreComptes?: number;
  comptes?: AccountResponse[];
}

export interface ClientRequest {
  nom: string;
  prenom: string;
  dateNaissance: string; // ISO date YYYY-MM-DD
  sexe: Sexe;
  adresse?: string;
  telephone?: string;
  courriel?: string;
  nationalite?: string;
}
