package com.example.todo.domain.model;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

/**
 * TodoId Value Object
 *
 * Représente l'identifiant unique d'un Todo selon les principes DDD.
 * Un Value Object est immutable et défini par sa valeur, pas son identité.
 *
 * Concepts DDD appliqués :
 * - Value Object : Immutable, égalité basée sur la valeur
 * - Type Safety : Évite les primitive obsessions
 * - Domain Language : Exprime clairement l'intention métier
 *
 * @author Todo Team
 */
@Embeddable
public record TodoId(String value) {

    public TodoId {
        Objects.requireNonNull(value, "TodoId value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TodoId value cannot be blank");
        }
        // Validation UUID format
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("TodoId must be a valid UUID format", e);
        }
    }

    /**
     * Génère un nouvel identifiant unique
     *
     * @return nouveau TodoId
     */
    public static TodoId generate() {
        return new TodoId(UUID.randomUUID().toString());
    }

    /**
     * Crée un TodoId à partir d'une chaîne
     *
     * @param value valeur de l'identifiant
     * @return TodoId
     */
    public static TodoId of(String value) {
        return new TodoId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
