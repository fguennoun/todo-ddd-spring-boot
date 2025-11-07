package com.example.todo.infrastructure.metrics;

import com.example.todo.domain.repository.MetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class TodoMetrics implements MetricsPort {
    private final Counter todoCreatedCounter;
    private final Counter todoCompletedCounter;
    private final Timer todoCreationTimer;

    public TodoMetrics(MeterRegistry registry) {
        this.todoCreatedCounter = Counter.builder("todos.created")
                .description("Number of todos created")
                .register(registry);

        this.todoCompletedCounter = Counter.builder("todos.completed")
                .description("Number of todos completed")
                .register(registry);

        this.todoCreationTimer = Timer.builder("todos.creation.time")
                .description("Time taken to create todos")
                .register(registry);
    }

    @Override
    public <T> T recordCreationTime(Supplier<T> operation) {
        return todoCreationTimer.record(operation);
    }

    @Override
    public void incrementTodoCreated() {
        todoCreatedCounter.increment();
    }

    @Override
    public void incrementTodoCompleted() {
        todoCompletedCounter.increment();
    }

    public Timer getTodoCreationTimer() {
        return todoCreationTimer;
    }
}
