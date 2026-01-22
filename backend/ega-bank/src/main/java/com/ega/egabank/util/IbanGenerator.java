package com.ega.egabank.util;

import java.security.SecureRandom;

// Using a lightweight IBAN-like generator to avoid unsupported country in iban4j
import org.springframework.stereotype.Component;

/**
 * Utilitaire pour la génération de numéros IBAN
 */
@Component
public class IbanGenerator {

    private static final String BANK_CODE = "EGA";
    private static final String BRANCH_CODE = "00001";
    private static final SecureRandom random = new SecureRandom();

    /**
     * Génère un numéro IBAN unique au format togolais
     */
    public String generate() {
        String accountNumber = generateAccountNumber();
        // Build a simple IBAN-like identifier: TG + 2-digit checksum + BANK + BRANCH + account
        // Note: iban4j does not support TG; using a deterministic simple format for dev/testing
        int check = random.nextInt(100);
        String checkDigits = String.format("%02d", check);
        return "TG" + checkDigits + BANK_CODE + BRANCH_CODE + accountNumber;
    }

    /**
     * Génère un numéro de compte aléatoire de 11 caractères
     */
    private String generateAccountNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Formate un IBAN pour l'affichage (groupes de 4 caractères)
     */
    public String formatForDisplay(String iban) {
        if (iban == null || iban.isEmpty()) {
            return iban;
        }
        return iban.replaceAll("(.{4})", "$1 ").trim();
    }

    /**
     * Vérifie si un IBAN est valide
     */
    public boolean isValid(String iban) {
        if (iban == null) return false;
        // Basic format check: 2 letters (country) + 2 digits (checksum) + BBAN (alphanumeric, 10-30 chars)
        return iban.matches("^[A-Z]{2}\\d{2}[A-Z0-9]{10,30}$");
    }
}
