package com.example.todo.domain.events;

import com.example.todo.domain.model.TodoId;
import java.time.Instant;

/**
 * TodoCompletedEvent
 *
 * Événement émis lors de la finalisation d'un Todo.
 * Déclenche des actions spécifiques à la complétion (notifications, métriques, etc.)
 *
 * @author Todo Team
 */
public record TodoCompletedEvent(
        TodoId aggregateId,
        String userId,
        Instant completedAt,
        Instant occurredOn
) implements DomainEvent {

    public TodoCompletedEvent {
        if (aggregateId == null) {
            throw new IllegalArgumentException("AggregateId cannot be null");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("UserId cannot be null or blank");
        }
        if (completedAt == null) {
            throw new IllegalArgumentException("CompletedAt cannot be null");
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
