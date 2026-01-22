package com.ega.egabank.enums;

/**
 * Types de transactions bancaires
 */
public enum TypeTransaction {
    DEPOT("Dépôt"),
    RETRAIT("Retrait"),
    VIREMENT_ENTRANT("Virement reçu"),
    VIREMENT_SORTANT("Virement émis");

    private final String libelle;

    TypeTransaction(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
