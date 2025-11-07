package com.example.todo.domain.model;

import com.example.todo.domain.events.TodoCompletedEvent;
import com.example.todo.domain.events.TodoCreatedEvent;
import com.example.todo.domain.events.TodoStatusChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires pour l'agrégat Todo
 *
 * Teste la logique métier et les invariants de l'agrégat.
 * Utilise AssertJ pour des assertions fluides et expressives.
 *
 * @author Todo Team
 */
@DisplayName("Todo Aggregate")
class TodoTest {

    private static final String USER_ID = "user123";
    private static final String TITLE = "Test Todo";
    private static final String DESCRIPTION = "Test Description";
    private static final TodoPriority PRIORITY = TodoPriority.NORMAL;

    @Nested
    @DisplayName("Creation")
    class CreationTests {

        @Test
        @DisplayName("Should create todo with valid data")
        void shouldCreateTodoWithValidData() {
            // Given
            Instant dueDate = Instant.now().plus(1, ChronoUnit.DAYS);

            // When
            Todo todo = Todo.create(TITLE, DESCRIPTION, PRIORITY, dueDate, USER_ID);

            // Then
            assertThat(todo.getId()).isNotNull();
            assertThat(todo.getTitle()).isEqualTo(TITLE);
            assertThat(todo.getDescription()).isEqualTo(DESCRIPTION);
            assertThat(todo.getStatus()).isEqualTo(TodoStatus.PENDING);
            assertThat(todo.getPriority()).isEqualTo(PRIORITY);
            assertThat(todo.getDueDate()).isEqualTo(dueDate);
            assertThat(todo.getUserId()).isEqualTo(USER_ID);
            assertThat(todo.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(todo.getUpdatedAt()).isEqualTo(todo.getCreatedAt());
            assertThat(todo.getCompletedAt()).isNull();
            assertThat(todo.isEditable()).isTrue();

            // Vérification de l'événement de création
            var events = todo.getAndClearDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(TodoCreatedEvent.class);

            TodoCreatedEvent event = (TodoCreatedEvent) events.get(0);
            assertThat(event.aggregateId()).isEqualTo(todo.getId());
            assertThat(event.title()).isEqualTo(TITLE);
            assertThat(event.userId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("Should reject null or blank title")
        void shouldRejectNullOrBlankTitle() {
            // Given & When & Then
            assertThatThrownBy(() -> Todo.create(null, DESCRIPTION, PRIORITY, null, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");

            assertThatThrownBy(() -> Todo.create("", DESCRIPTION, PRIORITY, null, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");

            assertThatThrownBy(() -> Todo.create("   ", DESCRIPTION, PRIORITY, null, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");
        }

        @Test
        @DisplayName("Should reject due date in the past")
        void shouldRejectDueDateInThePast() {
            // Given
            Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);

            // When & Then
            assertThatThrownBy(() -> Todo.create(TITLE, DESCRIPTION, PRIORITY, pastDate, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("past");
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionTests {

        private Todo createTestTodo() {
            return Todo.create(TITLE, DESCRIPTION, PRIORITY, null, USER_ID);
        }

        @Test
        @DisplayName("Should start todo from pending status")
        void shouldStartTodoFromPendingStatus() {
            // Given
            Todo todo = createTestTodo();
            todo.getAndClearDomainEvents(); // Clear creation event

            // When
            todo.start();

            // Then
            assertThat(todo.getStatus()).isEqualTo(TodoStatus.IN_PROGRESS);

            var events = todo.getAndClearDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(TodoStatusChangedEvent.class);

            TodoStatusChangedEvent event = (TodoStatusChangedEvent) events.get(0);
            assertThat(event.previousStatus()).isEqualTo(TodoStatus.PENDING);
            assertThat(event.newStatus()).isEqualTo(TodoStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("Should complete todo from in progress status")
        void shouldCompleteTodoFromInProgressStatus() {
            // Given
            Todo todo = createTestTodo();
            todo.start();
            todo.getAndClearDomainEvents(); // Clear previous events

            // When
            todo.complete();

            // Then
            assertThat(todo.getStatus()).isEqualTo(TodoStatus.COMPLETED);
            assertThat(todo.getCompletedAt()).isNotNull();
            assertThat(todo.isEditable()).isFalse();

            var events = todo.getAndClearDomainEvents();
            assertThat(events).hasSize(2); // StatusChanged + Completed events

            assertThat(events)
                .anyMatch(event -> event instanceof TodoStatusChangedEvent)
                .anyMatch(event -> event instanceof TodoCompletedEvent);
        }

        @Test
        @DisplayName("Should reject invalid status transitions")
        void shouldRejectInvalidStatusTransitions() {
            // Given
            Todo todo = createTestTodo();
            todo.complete();

            // When & Then - Cannot modify completed todo
            assertThatThrownBy(todo::start)
                .isInstanceOf(IllegalStateException.class);

            assertThatThrownBy(() -> todo.updateTitle("New Title"))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Updates")
    class UpdateTests {

        private Todo createTestTodo() {
            return Todo.create(TITLE, DESCRIPTION, PRIORITY, null, USER_ID);
        }

        @Test
        @DisplayName("Should update title successfully")
        void shouldUpdateTitleSuccessfully() {
            // Given
            Todo todo = createTestTodo();
            String newTitle = "Updated Title";
            Instant originalUpdatedAt = todo.getUpdatedAt();

            // When
            todo.updateTitle(newTitle);

            // Then
            assertThat(todo.getTitle()).isEqualTo(newTitle);
            assertThat(todo.getUpdatedAt()).isAfter(originalUpdatedAt);
        }

        @Test
        @DisplayName("Should update priority successfully")
        void shouldUpdatePrioritySuccessfully() {
            // Given
            Todo todo = createTestTodo();
            TodoPriority newPriority = TodoPriority.HIGH;

            // When
            todo.updatePriority(newPriority);

            // Then
            assertThat(todo.getPriority()).isEqualTo(newPriority);
        }

        @Test
        @DisplayName("Should update due date successfully")
        void shouldUpdateDueDateSuccessfully() {
            // Given
            Todo todo = createTestTodo();
            Instant newDueDate = Instant.now().plus(7, ChronoUnit.DAYS);

            // When
            todo.updateDueDate(newDueDate);

            // Then
            assertThat(todo.getDueDate()).isEqualTo(newDueDate);
        }
    }

    @Nested
    @DisplayName("Business Rules")
    class BusinessRulesTests {

        @Test
        @DisplayName("Should detect overdue todos")
        void shouldDetectOverdueTodos() {
            // Given
            Instant pastDate = Instant.now().minus(1, ChronoUnit.HOURS);
            Todo todo = new Todo(
                TodoId.generate(), TITLE, DESCRIPTION, TodoStatus.PENDING,
                PRIORITY, pastDate, USER_ID,
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(2, ChronoUnit.HOURS),
                null
            );

            // When & Then
            assertThat(todo.isOverdue()).isTrue();
        }

        @Test
        @DisplayName("Should not consider completed todos as overdue")
        void shouldNotConsiderCompletedTodosAsOverdue() {
            // Given
            Instant pastDate = Instant.now().minus(1, ChronoUnit.HOURS);
            Todo todo = new Todo(
                TodoId.generate(), TITLE, DESCRIPTION, TodoStatus.COMPLETED,
                PRIORITY, pastDate, USER_ID,
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().minus(1, ChronoUnit.HOURS)
            );

            // When & Then
            assertThat(todo.isOverdue()).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality and Hash Code")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            // Given
            TodoId id = TodoId.generate();
            Todo todo1 = new Todo(id, TITLE, DESCRIPTION, TodoStatus.PENDING, PRIORITY, null, USER_ID, Instant.now(), Instant.now(), null);
            Todo todo2 = new Todo(id, "Different Title", "Different Description", TodoStatus.IN_PROGRESS, TodoPriority.HIGH, null, "different-user", Instant.now(), Instant.now(), null);

            // When & Then
            assertThat(todo1).isEqualTo(todo2);
            assertThat(todo1.hashCode()).isEqualTo(todo2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different ID")
        void shouldNotBeEqualWhenDifferentId() {
            // Given
            Todo todo1 = Todo.create(TITLE, DESCRIPTION, PRIORITY, null, USER_ID);
            Todo todo2 = Todo.create(TITLE, DESCRIPTION, PRIORITY, null, USER_ID);

            // When & Then
            assertThat(todo1).isNotEqualTo(todo2);
        }
    }
}
