// Transaction type enum matching backend TypeTransaction
export type TypeTransaction = 'DEPOT' | 'RETRAIT' | 'VIREMENT_ENTRANT' | 'VIREMENT_SORTANT';

export interface TransactionResponse {
  id: number;
  type: TypeTransaction;
  typeLibelle?: string;
  montant: number;
  dateTransaction: string; // ISO datetime
  description?: string;
  compteDestination?: string;
  soldeAvant?: number;
  soldeApres?: number;
  numeroCompte?: string;
}

export interface OperationRequest {
  montant: number;
  description?: string;
}

export interface TransferRequest {
  compteSource: string;
  compteDestination: string;
  montant: number;
  description?: string;
}
