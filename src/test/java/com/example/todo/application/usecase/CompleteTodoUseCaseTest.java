package com.example.todo.application.usecase;

import com.example.todo.application.dto.TodoResponse;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.TodoPriority;
import com.example.todo.domain.model.TodoStatus;
import com.example.todo.domain.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour CompleteTodoUseCase
 *
 * @author Todo Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Complete Todo Use Case")
class CompleteTodoUseCaseTest {

    @Mock
    private TodoRepository todoRepository;

    private CompleteTodoUseCase useCase;

    private static final String USER_ID = "user123";

    @BeforeEach
    void setUp() {
        useCase = new CompleteTodoUseCase(todoRepository);
    }

    @Test
    @DisplayName("Should complete todo successfully")
    void shouldCompleteTodoSuccessfully() {
        // Given
        String title = "Test Todo";
        String description = "Description";
        TodoPriority priority = TodoPriority.HIGH;
        Instant dueDate = Instant.now().plusSeconds(86400); // +1 day

        Todo todo = Todo.create(title, description, priority, dueDate, USER_ID);
        String todoId = todo.getId().value();

        when(todoRepository.findById(any(TodoId.class))).thenReturn(Optional.of(todo));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        // When
        TodoResponse result = useCase.execute(todoId, USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(TodoStatus.COMPLETED);
        assertThat(result.title()).isEqualTo(title);
        verify(todoRepository).findById(any(TodoId.class));
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    @DisplayName("Should throw exception when todo not found")
    void shouldThrowExceptionWhenTodoNotFound() {
        // Given
        String todoId = TodoId.generate().value();
        when(todoRepository.findById(any(TodoId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(todoId, USER_ID))
            .isInstanceOf(TodoNotFoundException.class)
            .hasMessageContaining("Todo not found");

        verify(todoRepository).findById(any(TodoId.class));
    }

    @Test
    @DisplayName("Should throw exception when user does not own the todo")
    void shouldThrowExceptionWhenUserDoesNotOwnTheTodo() {
        // Given
        String title = "Test Todo";
        String description = "Description";
        TodoPriority priority = TodoPriority.NORMAL;
        Instant dueDate = Instant.now().plusSeconds(86400);

        String ownerUserId = "owner123";
        String otherUserId = "other456";

        Todo todo = Todo.create(title, description, priority, dueDate, ownerUserId);
        String todoId = todo.getId().value();

        when(todoRepository.findById(any(TodoId.class))).thenReturn(Optional.of(todo));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(todoId, otherUserId))
            .isInstanceOf(TodoNotFoundException.class)
            .hasMessageContaining("Todo not found");

        verify(todoRepository).findById(any(TodoId.class));
    }
}

