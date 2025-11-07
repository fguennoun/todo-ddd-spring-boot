package com.example.todo.infrastructure.persistence;

import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.TodoPriority;
import com.example.todo.domain.model.TodoStatus;
import com.example.todo.domain.repository.TodoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration du repository avec Testcontainers
 *
 * Utilise PostgreSQL dans un container Docker pour des tests réalistes.
 *
 * @author Todo Team
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TodoRepositoryImpl.class})
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
@DisplayName("Todo Repository Integration Tests")
class TodoRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("todotest")
        .withUsername("test")
        .withPassword("test");

    static {
        postgres.start();
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }

    @Autowired
    private TodoRepository todoRepository;

    private static final String USER_ID = "user123";

    @Test
    @DisplayName("Should save and retrieve todo")
    void shouldSaveAndRetrieveTodo() {
        // Given
        Todo todo = Todo.create(
            "Test Todo",
            "Test Description",
            TodoPriority.NORMAL,
            Instant.now().plus(1, ChronoUnit.DAYS),
            USER_ID
        );

        // When
        Todo savedTodo = todoRepository.save(todo);
        Optional<Todo> retrieved = todoRepository.findById(savedTodo.getId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getTitle()).isEqualTo("Test Todo");
        assertThat(retrieved.get().getUserId()).isEqualTo(USER_ID);
        assertThat(retrieved.get().getStatus()).isEqualTo(TodoStatus.PENDING);
    }

    @Test
    @DisplayName("Should find todos by user with pagination")
    void shouldFindTodosByUserWithPagination() {
        // Given - Create multiple todos
        for (int i = 1; i <= 5; i++) {
            Todo todo = Todo.create(
                "Todo " + i,
                "Description " + i,
                TodoPriority.NORMAL,
                null,
                USER_ID
            );
            todoRepository.save(todo);
        }

        // When
        Page<Todo> page = todoRepository.findByUserId(USER_ID, PageRequest.of(0, 3));

        // Then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find overdue todos")
    void shouldFindOverdueTodos() {
        // Given
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);

        // Create overdue todo
        Todo overdueTodo = new Todo(
            TodoId.generate(),
            "Overdue Todo",
            "This is overdue",
            TodoStatus.PENDING,
            TodoPriority.HIGH,
            pastDate,
            USER_ID,
            Instant.now().minus(2, ChronoUnit.DAYS),
            Instant.now().minus(2, ChronoUnit.DAYS),
            null
        );
        todoRepository.save(overdueTodo);

        // Create non-overdue todo
        Todo futureTodo = Todo.create(
            "Future Todo",
            "This is not overdue",
            TodoPriority.NORMAL,
            Instant.now().plus(1, ChronoUnit.DAYS),
            USER_ID
        );
        todoRepository.save(futureTodo);

        // When
        List<Todo> overdueTodos = todoRepository.findOverdueTodosByUserId(USER_ID, Instant.now());

        // Then
        assertThat(overdueTodos).hasSize(1);
        assertThat(overdueTodos.get(0).getTitle()).isEqualTo("Overdue Todo");
    }

    @Test
    @DisplayName("Should count todos by status")
    void shouldCountTodosByStatus() {
        // Given
        Todo pendingTodo = Todo.create("Pending", "Description", TodoPriority.NORMAL, null, USER_ID);
        todoRepository.save(pendingTodo);

        Todo inProgressTodo = Todo.create("In Progress", "Description", TodoPriority.NORMAL, null, USER_ID);
        inProgressTodo.start();
        todoRepository.save(inProgressTodo);

        // When
        long pendingCount = todoRepository.countByUserIdAndStatus(USER_ID, TodoStatus.PENDING);
        long inProgressCount = todoRepository.countByUserIdAndStatus(USER_ID, TodoStatus.IN_PROGRESS);

        // Then
        assertThat(pendingCount).isEqualTo(1);
        assertThat(inProgressCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should delete todo by id")
    void shouldDeleteTodoById() {
        // Given
        Todo todo = Todo.create("To Delete", "Description", TodoPriority.NORMAL, null, USER_ID);
        Todo savedTodo = todoRepository.save(todo);

        // When
        todoRepository.deleteById(savedTodo.getId());

        // Then
        Optional<Todo> deleted = todoRepository.findById(savedTodo.getId());
        assertThat(deleted).isEmpty();
    }
}
