// Account type enum matching backend TypeCompte
export type TypeCompte = 'EPARGNE' | 'COURANT';

export interface AccountResponse {
  id: number;
  numeroCompte: string;
  typeCompte: TypeCompte;
  typeCompteLibelle?: string;
  dateCreation?: string; // ISO datetime
  solde: number;
  actif: boolean;
  clientId?: number;
  clientNomComplet?: string;
}

export interface AccountRequest {
  typeCompte: TypeCompte;
  clientId: number;
}
