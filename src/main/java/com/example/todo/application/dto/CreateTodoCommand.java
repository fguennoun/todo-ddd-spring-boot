package com.example.todo.application.dto;

import com.example.todo.domain.model.TodoPriority;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * CreateTodoCommand
 *
 * Commande pour la création d'un nouveau Todo.
 * Encapsule les données nécessaires à la création et les règles de validation.
 *
 * Concepts DDD appliqués :
 * - Command Pattern : Encapsulation d'une requête comme objet
 * - Data Transfer Object : Transport de données entre couches
 * - Validation : Règles de validation des données d'entrée
 *
 * @author Todo Team
 */
@Schema(description = "Commande pour créer un nouveau Todo")
public record CreateTodoCommand(

    @Schema(description = "Titre du Todo", example = "Implémenter les tests unitaires")
    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 1, max = 255, message = "Le titre doit contenir entre 1 et 255 caractères")
    String title,

    @Schema(description = "Description détaillée du Todo", example = "Créer les tests unitaires pour la couche domaine")
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    String description,

    @Schema(description = "Niveau de priorité du Todo")
    @NotNull(message = "La priorité est obligatoire")
    TodoPriority priority,

    @Schema(description = "Date d'échéance du Todo", example = "2024-12-31T23:59:59Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant dueDate
) {

    public CreateTodoCommand {
        // Validation personnalisée
        if (title != null) {
            title = title.trim();
        }
        if (description != null && !description.isBlank()) {
            description = description.trim();
        }
    }
}
