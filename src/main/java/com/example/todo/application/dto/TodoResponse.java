package com.example.todo.application.dto;

import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.TodoPriority;
import com.example.todo.domain.model.TodoStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * TodoResponse
 *
 * DTO pour la représentation d'un Todo dans les réponses API.
 * Contient toutes les informations nécessaires pour l'affichage côté client.
 *
 * @author Todo Team
 */
@Schema(description = "Représentation d'un Todo")
public record TodoResponse(

    @Schema(description = "Identifiant unique du Todo", example = "550e8400-e29b-41d4-a716-446655440000")
    String id,

    @Schema(description = "Titre du Todo", example = "Implémenter les tests unitaires")
    String title,

    @Schema(description = "Description du Todo", example = "Créer les tests unitaires pour la couche domaine")
    String description,

    @Schema(description = "Statut actuel du Todo")
    TodoStatus status,

    @Schema(description = "Priorité du Todo")
    TodoPriority priority,

    @Schema(description = "Date d'échéance du Todo", example = "2024-12-31T23:59:59Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant dueDate,

    @Schema(description = "Identifiant du propriétaire", example = "user123")
    String userId,

    @Schema(description = "Date de création", example = "2024-01-15T10:30:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant createdAt,

    @Schema(description = "Date de dernière modification", example = "2024-01-15T15:45:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant updatedAt,

    @Schema(description = "Date de complétion (si complété)", example = "2024-01-16T09:20:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant completedAt,

    @Schema(description = "Indique si le Todo est en retard")
    boolean overdue,

    @Schema(description = "Indique si le Todo peut être modifié")
    boolean editable
) {

    /**
     * Crée une TodoResponse à partir d'un Todo du domaine
     *
     * @param todo Todo du domaine
     * @return TodoResponse
     */
    public static TodoResponse from(com.example.todo.domain.model.Todo todo) {
        return new TodoResponse(
            todo.getId().value(),
            todo.getTitle(),
            todo.getDescription(),
            todo.getStatus(),
            todo.getPriority(),
            todo.getDueDate(),
            todo.getUserId(),
            todo.getCreatedAt(),
            todo.getUpdatedAt(),
            todo.getCompletedAt(),
            todo.isOverdue(),
            todo.isEditable()
        );
    }
}
