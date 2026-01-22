package com.ega.egabank.enums;

/**
 * Types de comptes bancaires disponibles
 */
public enum TypeCompte {
    EPARGNE("Compte Épargne"),
    COURANT("Compte Courant");

    private final String libelle;

    TypeCompte(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
