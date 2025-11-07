package com.example.todo.application.usecase;

import com.example.todo.application.dto.CreateTodoCommand;
import com.example.todo.application.dto.TodoResponse;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoPriority;
import com.example.todo.domain.repository.TodoRepository;
import com.example.todo.domain.service.TodoDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CreateTodoUseCase
 *
 * @author Todo Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Create Todo Use Case")
class CreateTodoUseCaseTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TodoDomainService todoDomainService;

    private CreateTodoUseCase useCase;

    private static final String USER_ID = "user123";

    @BeforeEach
    void setUp() {
        useCase = new CreateTodoUseCase(todoRepository, todoDomainService);
    }

    @Test
    @DisplayName("Should create todo successfully when user can create new todos")
    void shouldCreateTodoSuccessfullyWhenUserCanCreateNewTodos() {
        // Given
        CreateTodoCommand command = new CreateTodoCommand(
            "Test Todo",
            "Test Description",
            TodoPriority.NORMAL,
            Instant.now().plus(1, ChronoUnit.DAYS)
        );

        Todo expectedTodo = Todo.create(
            command.title(),
            command.description(),
            command.priority(),
            command.dueDate(),
            USER_ID
        );

        when(todoDomainService.canCreateNewTodo(USER_ID)).thenReturn(true);
        when(todoRepository.save(any(Todo.class))).thenReturn(expectedTodo);

        // When
        TodoResponse result = useCase.execute(command, USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(command.title());
        assertThat(result.description()).isEqualTo(command.description());
        assertThat(result.priority()).isEqualTo(command.priority());
        assertThat(result.dueDate()).isEqualTo(command.dueDate());
        assertThat(result.userId()).isEqualTo(USER_ID);

        verify(todoDomainService).canCreateNewTodo(USER_ID);
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    @DisplayName("Should throw exception when user has reached todo limit")
    void shouldThrowExceptionWhenUserHasReachedTodoLimit() {
        // Given
        CreateTodoCommand command = new CreateTodoCommand(
            "Test Todo",
            "Test Description",
            TodoPriority.NORMAL,
            null
        );

        when(todoDomainService.canCreateNewTodo(USER_ID)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command, USER_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("maximum number of active todos");

        verify(todoDomainService).canCreateNewTodo(USER_ID);
        verify(todoRepository, never()).save(any());
    }
}
