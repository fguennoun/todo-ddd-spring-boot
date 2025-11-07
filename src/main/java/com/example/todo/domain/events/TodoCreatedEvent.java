package com.example.todo.domain.events;

import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.TodoPriority;
import java.time.Instant;

/**
 * TodoCreatedEvent
 *
 * Événement émis lors de la création d'un nouveau Todo.
 * Contient toutes les informations nécessaires aux handlers d'événements.
 *
 * Concepts DDD appliqués :
 * - Domain Event : Événement métier significatif
 * - Immutability : Événement immutable (record)
 * - Rich Event : Contient toutes les données nécessaires
 *
 * @author Todo Team
 */
public record TodoCreatedEvent(
        TodoId aggregateId,
        String title,
        String description,
        TodoPriority priority,
        Instant dueDate,
        String userId,
        Instant occurredOn
) implements DomainEvent {

    public TodoCreatedEvent {
        if (aggregateId == null) {
            throw new IllegalArgumentException("AggregateId cannot be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("UserId cannot be null or blank");
        }
        if (occurredOn == null) {
            throw new IllegalArgumentException("OccurredOn cannot be null");
        }
    }

    @Override
    public TodoId getAggregateId() {
        return aggregateId;
    }

    @Override
    public Instant getOccurredOn() {
        return occurredOn;
    }
}
