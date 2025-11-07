package com.example.todo.application.usecase;

import com.example.todo.application.dto.TodoResponse;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CompleteTodoUseCase
 *
 * Use Case pour marquer un Todo comme complété.
 * Déclenche les événements métier associés à la complétion.
 *
 * @author Todo Team
 */
@Service
@Transactional
public class CompleteTodoUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CompleteTodoUseCase.class);

    private final TodoRepository todoRepository;

    public CompleteTodoUseCase(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Marque un Todo comme complété
     *
     * @param todoId identifiant du Todo
     * @param userId identifiant de l'utilisateur
     * @return Todo complété
     * @throws TodoNotFoundException si le Todo n'existe pas
     * @throws IllegalStateException si le Todo ne peut pas être complété
     */
    @CacheEvict(value = "todos", key = "#todoId")
    public TodoResponse execute(String todoId, String userId) {
        logger.info("Completing todo with id: {} for user: {}", todoId, userId);

        TodoId id = TodoId.of(todoId);

        Todo todo = todoRepository.findById(id)
            .filter(t -> t.getUserId().equals(userId))
            .orElseThrow(() -> new TodoNotFoundException("Todo not found with id: " + todoId));

        // Complétion via la méthode métier de l'agrégat
        todo.complete();

        Todo savedTodo = todoRepository.save(todo);

        logger.info("Todo completed successfully with id: {}", savedTodo.getId());

        return TodoResponse.from(savedTodo);
    }
}
