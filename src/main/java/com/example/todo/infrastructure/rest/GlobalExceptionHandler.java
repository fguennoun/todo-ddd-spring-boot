package com.example.todo.infrastructure.rest;

import com.example.todo.application.usecase.TodoNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler
 *
 * Gestionnaire global des exceptions suivant la RFC 7807 (Problem Details for HTTP APIs).
 * Fournit des réponses d'erreur standardisées et cohérentes.
 *
 * @author Todo Team
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Gestion des erreurs de validation (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        logger.warn("Validation error: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Les données fournies ne respectent pas les règles de validation"
        );

        problemDetail.setTitle("Erreur de validation");
        problemDetail.setType(URI.create("https://api.todo-app.com/problems/validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        // Détails des erreurs de validation
        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }
        problemDetail.setProperty("validationErrors", validationErrors);

        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Gestion des erreurs "Todo non trouvé"
     */
    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTodoNotFoundException(
            TodoNotFoundException ex, WebRequest request) {

        logger.warn("Todo not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );

        problemDetail.setTitle("Todo non trouvé");
        problemDetail.setType(URI.create("https://api.todo-app.com/problems/todo-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * Gestion des erreurs d'état illégal (règles métier violées)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        logger.warn("Business rule violation: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );

        problemDetail.setTitle("Règle métier violée");
        problemDetail.setType(URI.create("https://api.todo-app.com/problems/business-rule-violation"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    /**
     * Gestion des arguments illégaux
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        logger.warn("Invalid argument: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );

        problemDetail.setTitle("Argument invalide");
        problemDetail.setType(URI.create("https://api.todo-app.com/problems/invalid-argument"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Gestion des erreurs d'accès non autorisé
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ProblemDetail> handleSecurityException(
            SecurityException ex, WebRequest request) {

        logger.warn("Security violation: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "Accès non autorisé à cette ressource"
        );

        problemDetail.setTitle("Accès interdit");
        problemDetail.setType(URI.create("https://api.todo-app.com/problems/access-denied"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    /**
     * Gestion générale des erreurs internes
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, WebRequest request) {

        logger.error("Unexpected error occurred", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Une erreur inattendue s'est produite. Veuillez réessayer plus tard."
        );

        problemDetail.setTitle("Erreur interne du serveur");
        problemDetail.setType(URI.create("https://api.todo-app.com/problems/internal-server-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        // En développement, on peut ajouter plus de détails
        if (logger.isDebugEnabled()) {
            problemDetail.setProperty("exceptionClass", ex.getClass().getSimpleName());
            problemDetail.setProperty("exceptionMessage", ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}
