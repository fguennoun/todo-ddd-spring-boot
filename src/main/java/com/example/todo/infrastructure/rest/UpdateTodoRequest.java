package com.example.todo.infrastructure.rest;

import com.example.todo.domain.model.Priority;
import java.time.LocalDateTime;

public record UpdateTodoRequest(
    String title,
    String description,
    Priority priority,
    LocalDateTime dueDate
) {}
