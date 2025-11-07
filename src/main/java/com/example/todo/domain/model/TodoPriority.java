package com.example.todo.domain.model;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * TodoPriority Value Object
 *
 * Représente le niveau de priorité d'un Todo.
 * Encapsule la logique métier liée aux priorités.
 *
 * Concepts DDD appliqués :
 * - Value Object : Immutable, comparaison par valeur
 * - Domain Logic : Logique de comparaison des priorités
 * - Type Safety : Évite les erreurs de type primitif
 *
 * @author Todo Team
 */
@Embeddable
public record TodoPriority(int level, String name) implements Comparable<TodoPriority> {

    // Constantes pour les priorités standards
    public static final TodoPriority LOW = new TodoPriority(1, "Basse");
    public static final TodoPriority NORMAL = new TodoPriority(2, "Normale");
    public static final TodoPriority HIGH = new TodoPriority(3, "Haute");
    public static final TodoPriority CRITICAL = new TodoPriority(4, "Critique");

    public TodoPriority {
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("Priority level must be between 1 and 4");
        }
        Objects.requireNonNull(name, "Priority name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Priority name cannot be blank");
        }
    }

    /**
     * Crée une priorité à partir du niveau
     *
     * @param level niveau de priorité (1-4)
     * @return TodoPriority correspondant
     */
    public static TodoPriority fromLevel(int level) {
        return switch (level) {
            case 1 -> LOW;
            case 2 -> NORMAL;
            case 3 -> HIGH;
            case 4 -> CRITICAL;
            default -> throw new IllegalArgumentException("Invalid priority level: " + level);
        };
    }

    /**
     * Vérifie si cette priorité est plus haute que l'autre
     *
     * @param other autre priorité
     * @return true si cette priorité est plus haute
     */
    public boolean isHigherThan(TodoPriority other) {
        return this.level > other.level;
    }

    /**
     * Vérifie si c'est une priorité critique
     *
     * @return true si critique
     */
    public boolean isCritical() {
        return this.level == CRITICAL.level;
    }

    @Override
    public int compareTo(TodoPriority other) {
        return Integer.compare(this.level, other.level);
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", name, level);
    }
}
