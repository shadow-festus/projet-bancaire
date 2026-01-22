package com.ega.egabank.exception;

/**
 * Exception levée lors d'un échec de génération de relevé de compte
 */
public class StatementGenerationException extends RuntimeException {

    public StatementGenerationException(String message) {
        super(message);
    }

    public StatementGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
