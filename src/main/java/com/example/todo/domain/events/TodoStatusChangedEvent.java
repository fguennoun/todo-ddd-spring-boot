package com.example.todo.domain.events;

import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.TodoStatus;
import java.time.Instant;

/**
 * TodoStatusChangedEvent
 *
 * Événement émis lors du changement de statut d'un Todo.
 * Permet de tracer les transitions d'état et déclencher des actions.
 *
 * @author Todo Team
 */
public record TodoStatusChangedEvent(
        TodoId aggregateId,
        TodoStatus previousStatus,
        TodoStatus newStatus,
        String userId,
        Instant occurredOn
) implements DomainEvent {

    public TodoStatusChangedEvent {
        if (aggregateId == null) {
            throw new IllegalArgumentException("AggregateId cannot be null");
        }
        if (previousStatus == null) {
            throw new IllegalArgumentException("PreviousStatus cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("NewStatus cannot be null");
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
