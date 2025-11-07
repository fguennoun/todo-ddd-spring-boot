package com.example.todo.application.dto;

import com.example.todo.domain.model.TodoPriority;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * UpdateTodoCommand
 *
 * Commande pour la mise à jour d'un Todo existant.
 * Tous les champs sont optionnels pour permettre des mises à jour partielles.
 *
 * @author Todo Team
 */
@Schema(description = "Commande pour mettre à jour un Todo existant")
public record UpdateTodoCommand(

    @Schema(description = "Nouveau titre du Todo", example = "Implémenter les tests d'intégration")
    @Size(min = 1, max = 255, message = "Le titre doit contenir entre 1 et 255 caractères")
    String title,

    @Schema(description = "Nouvelle description du Todo")
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    String description,

    @Schema(description = "Nouvelle priorité du Todo")
    TodoPriority priority,

    @Schema(description = "Nouvelle date d'échéance du Todo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant dueDate
) {

    public UpdateTodoCommand {
        // Nettoyage des chaînes si elles ne sont pas nulles
        if (title != null && !title.isBlank()) {
            title = title.trim();
        }
        if (description != null && !description.isBlank()) {
            description = description.trim();
        }
    }

    /**
     * Vérifie si la commande contient au moins une modification
     *
     * @return true s'il y a au moins un champ à modifier
     */
    public boolean hasChanges() {
        return title != null || description != null || priority != null || dueDate != null;
    }
}
