package com.example.todo.infrastructure.rest;

import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoPriority;
import com.example.todo.domain.model.TodoStatus;

import java.time.Instant;

public record TodoResponse(
    String id,
    String title,
    String description,
    TodoStatus status,
    TodoPriority priority,
    Instant dueDate,
    Instant createdAt,
    Instant updatedAt
) {
    public static TodoResponse from(Todo todo) {
        return new TodoResponse(
            todo.getId().value(),
            todo.getTitle(),
            todo.getDescription(),
            todo.getStatus(),
            todo.getPriority(),
            todo.getDueDate(),
            todo.getCreatedAt(),
            todo.getUpdatedAt()
        );
    }
}
