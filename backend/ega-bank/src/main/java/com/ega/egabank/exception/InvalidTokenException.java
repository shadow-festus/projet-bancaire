package com.ega.egabank.exception;

/**
 * Exception levée quand un token d'authentification est invalide
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String tokenType, String reason) {
        super(String.format("Token %s invalide : %s", tokenType, reason));
    }
}
