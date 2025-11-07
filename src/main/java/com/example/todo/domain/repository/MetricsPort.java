package com.example.todo.domain.repository;

import java.util.function.Supplier;

/**
 * Port for metrics tracking
 * Allows domain to remain independent of infrastructure concerns
 */
public interface MetricsPort {
    /**
     * Record execution time of an operation
     */
    <T> T recordCreationTime(Supplier<T> operation);

    /**
     * Increment todo created counter
     */
    void incrementTodoCreated();

    /**
     * Increment todo completed counter
     */
    void incrementTodoCompleted();
}

