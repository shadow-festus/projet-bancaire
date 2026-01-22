package com.ega.egabank.exception;

/**
 * Exception levée quand le solde est insuffisant pour une opération
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String numeroCompte, java.math.BigDecimal soldeActuel,
            java.math.BigDecimal montantDemande) {
        super(String.format("Solde insuffisant sur le compte %s. Solde: %.2f, Montant demandé: %.2f",
                numeroCompte, soldeActuel, montantDemande));
    }
}
