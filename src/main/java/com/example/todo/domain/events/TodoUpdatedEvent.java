package com.example.todo.domain.events;

import com.example.todo.domain.model.TodoId;

import java.time.LocalDateTime;

public record TodoUpdatedEvent(TodoId todoId, LocalDateTime occurredAt) {}
