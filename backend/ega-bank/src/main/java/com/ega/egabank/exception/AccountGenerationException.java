package com.ega.egabank.exception;

/**
 * Exception levée lors d'un échec de génération de numéro de compte
 */
public class AccountGenerationException extends RuntimeException {

    public AccountGenerationException(String message) {
        super(message);
    }

    public AccountGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
