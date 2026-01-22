// Status utilities for account and transaction display

export function statusClassObject(actif: boolean) {
  return {
    'badge-success': actif,
    'badge-danger': !actif,
  } as Record<string, boolean>;
}

export function statusDisplay(actif: boolean) {
  return actif ? 'Active' : 'Inactive';
}

// Backend transaction types
export type TransactionType = 'DEPOT' | 'RETRAIT' | 'VIREMENT_ENTRANT' | 'VIREMENT_SORTANT';

export function transactionAmountClass(type: TransactionType) {
  const isPositive = type === 'DEPOT' || type === 'VIREMENT_ENTRANT';
  return {
    'text-danger': !isPositive,
    'text-success': isPositive,
  } as Record<string, boolean>;
}

export function transactionSign(type: TransactionType) {
  const isPositive = type === 'DEPOT' || type === 'VIREMENT_ENTRANT';
  return isPositive ? '+' : '-';
}

export function transactionTypeDisplay(type: TransactionType) {
  const displays: Record<TransactionType, string> = {
    DEPOT: 'Deposit',
    RETRAIT: 'Withdrawal',
    VIREMENT_ENTRANT: 'Transfer In',
    VIREMENT_SORTANT: 'Transfer Out',
  };
  return displays[type] || type;
}
