package com.ega.egabank.exception;

/**
 * Exception levée pour les opérations non autorisées
 */
public class OperationNotAllowedException extends RuntimeException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
