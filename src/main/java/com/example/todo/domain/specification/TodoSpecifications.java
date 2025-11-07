package com.example.todo.domain.specification;

import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoPriority;
import com.example.todo.domain.model.TodoStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * TodoSpecifications
 *
 * Catalogue de spécifications métier prédéfinies pour les Todos.
 * Fournit un vocabulaire métier riche et réutilisable.
 *
 * @author Todo Team
 */
public final class TodoSpecifications {

    private TodoSpecifications() {
        // Utility class
    }

    /**
     * Spécification pour les Todos en retard
     */
    public static Specification<Todo> isOverdue() {
        return Todo::isOverdue;
    }

    /**
     * Spécification pour les Todos avec une priorité donnée
     */
    public static Specification<Todo> hasPriority(TodoPriority priority) {
        return todo -> todo.getPriority().equals(priority);
    }

    /**
     * Spécification pour les Todos avec un statut donné
     */
    public static Specification<Todo> hasStatus(TodoStatus status) {
        return todo -> todo.getStatus() == status;
    }

    /**
     * Spécification pour les Todos actifs (non finalisés)
     */
    public static Specification<Todo> isActive() {
        return todo -> !todo.getStatus().isFinal();
    }

    /**
     * Spécification pour les Todos complétés
     */
    public static Specification<Todo> isCompleted() {
        return hasStatus(TodoStatus.COMPLETED);
    }

    /**
     * Spécification pour les Todos avec priorité critique
     */
    public static Specification<Todo> isCritical() {
        return todo -> todo.getPriority().isCritical();
    }

    /**
     * Spécification pour les Todos dus aujourd'hui
     */
    public static Specification<Todo> isDueToday() {
        return todo -> {
            if (todo.getDueDate() == null) {
                return false;
            }

            Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
            Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

            return !todo.getDueDate().isBefore(startOfDay) &&
                   todo.getDueDate().isBefore(endOfDay);
        };
    }

    /**
     * Spécification pour les Todos dus dans les X prochains jours
     */
    public static Specification<Todo> isDueWithinDays(int days) {
        return todo -> {
            if (todo.getDueDate() == null) {
                return false;
            }

            Instant futureLimit = Instant.now().plus(days, ChronoUnit.DAYS);
            return !todo.getDueDate().isAfter(futureLimit) &&
                   todo.getDueDate().isAfter(Instant.now());
        };
    }

    /**
     * Spécification pour les Todos créés récemment (dernières 24h)
     */
    public static Specification<Todo> isRecentlyCreated() {
        return todo -> {
            Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
            return todo.getCreatedAt().isAfter(oneDayAgo);
        };
    }

    /**
     * Spécification pour les Todos modifiés récemment
     */
    public static Specification<Todo> isRecentlyUpdated() {
        return todo -> {
            Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
            return todo.getUpdatedAt().isAfter(oneHourAgo);
        };
    }

    /**
     * Spécification composite : Todos urgents
     * (critiques ET dus dans les 2 prochains jours)
     */
    public static Specification<Todo> isUrgent() {
        return isCritical().and(isDueWithinDays(2));
    }

    /**
     * Spécification composite : Todos nécessitant attention
     * (en retard OU urgents)
     */
    public static Specification<Todo> needsAttention() {
        return isOverdue().or(isUrgent());
    }
}
