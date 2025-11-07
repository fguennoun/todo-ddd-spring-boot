package com.example.todo.infrastructure.rest;

import com.example.todo.domain.model.Priority;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTodoRequest(
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    String title,

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    @NotNull(message = "Priority is required")
    Priority priority,

    @NotNull(message = "Due date is required")
    LocalDateTime dueDate
) {}
