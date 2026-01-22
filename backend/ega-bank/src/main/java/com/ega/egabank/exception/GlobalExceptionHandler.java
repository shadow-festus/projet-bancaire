package com.ega.egabank.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Gestionnaire global des exceptions pour l'API
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiError> handleResourceNotFound(
                        ResourceNotFoundException ex, HttpServletRequest request) {
                log.warn("Resource non trouvée: {}", ex.getMessage());
                ApiError error = ApiError.of(
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(InsufficientBalanceException.class)
        public ResponseEntity<ApiError> handleInsufficientBalance(
                        InsufficientBalanceException ex, HttpServletRequest request) {
                log.warn("Solde insuffisant: {}", ex.getMessage());
                ApiError error = ApiError.of(
                                HttpStatus.BAD_REQUEST.value(),
                                "Solde Insuffisant",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiError> handleDuplicateResource(
                        DuplicateResourceException ex, HttpServletRequest request) {
                log.warn("Ressource dupliquée: {}", ex.getMessage());
                ApiError error = ApiError.of(
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        @ExceptionHandler(OperationNotAllowedException.class)
        public ResponseEntity<ApiError> handleOperationNotAllowed(
                        OperationNotAllowedException ex, HttpServletRequest request) {
                log.warn("Opération non autorisée: {}", ex.getMessage());
                ApiError error = ApiError.of(
                                HttpStatus.BAD_REQUEST.value(),
                                "Operation Not Allowed",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidationErrors(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {
                Map<String, String> validationErrors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String message = error.getDefaultMessage();
                        validationErrors.put(fieldName, message);
                });
                log.warn("Erreurs de validation: {}", validationErrors);
                ApiError error = ApiError.withValidation(
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Failed",
                                "Les données fournies ne sont pas valides",
                                request.getRequestURI(),
                                validationErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiError> handleBadCredentials(
                        BadCredentialsException ex, HttpServletRequest request) {
                log.warn("Identifiants invalides: {}", ex.getMessage());
                ApiError error = ApiError.of(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Identifiants invalides",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiError> handleAuthentication(
                        AuthenticationException ex, HttpServletRequest request) {
                log.warn("Erreur d'authentification: {}", ex.getMessage());
                ApiError error = ApiError.of(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Authentification requise",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> handleAccessDenied(
                        AccessDeniedException ex, HttpServletRequest request) {
                log.warn("Accès refusé: {}", ex.getMessage());
                ApiError error = ApiError.of(
                                HttpStatus.FORBIDDEN.value(),
                                "Forbidden",
                                "Accès refusé",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<ApiError> handleInvalidToken(
                        InvalidTokenException ex, HttpServletRequest request) {
                log.warn("Token invalide: {}", ex.getMessage());
                ApiError error = ApiError.of(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Token Invalide",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(AccountGenerationException.class)
        public ResponseEntity<ApiError> handleAccountGeneration(
                        AccountGenerationException ex, HttpServletRequest request) {
                log.error("Erreur de génération de compte: {}", ex.getMessage());
                ApiError error = ApiError.of(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Erreur de Génération",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        @ExceptionHandler(StatementGenerationException.class)
        public ResponseEntity<ApiError> handleStatementGeneration(
                        StatementGenerationException ex, HttpServletRequest request) {
                log.error("Erreur de génération de relevé: {}", ex.getMessage());
                ApiError error = ApiError.of(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Erreur de Génération de Relevé",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGenericException(
                        Exception ex, HttpServletRequest request) {
                log.error("Erreur inattendue", ex);
                ApiError error = ApiError.of(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "Une erreur inattendue s'est produite",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
}
