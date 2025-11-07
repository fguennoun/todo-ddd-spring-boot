package com.example.todo.application.usecase;

import com.example.todo.application.dto.TodoResponse;
import com.example.todo.application.dto.UpdateTodoCommand;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UpdateTodoUseCase
 *
 * Use Case pour la mise à jour d'un Todo existant.
 * Gère les mises à jour partielles et la validation des règles métier.
 *
 * @author Todo Team
 */
@Service
@Transactional
public class UpdateTodoUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateTodoUseCase.class);

    private final TodoRepository todoRepository;

    public UpdateTodoUseCase(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Met à jour un Todo existant
     *
     * @param todoId identifiant du Todo à modifier
     * @param command données de modification
     * @param userId identifiant de l'utilisateur
     * @return Todo mis à jour
     * @throws TodoNotFoundException si le Todo n'existe pas
     * @throws IllegalStateException si le Todo ne peut pas être modifié
     */
    @CacheEvict(value = "todos", key = "#todoId")
    public TodoResponse execute(String todoId, UpdateTodoCommand command, String userId) {
        logger.info("Updating todo with id: {} for user: {}", todoId, userId);

        if (!command.hasChanges()) {
            throw new IllegalArgumentException("No changes provided in update command");
        }

        TodoId id = TodoId.of(todoId);

        Todo todo = todoRepository.findById(id)
            .filter(t -> t.getUserId().equals(userId))
            .orElseThrow(() -> new TodoNotFoundException("Todo not found with id: " + todoId));

        // Vérification que le Todo peut être modifié
        if (!todo.isEditable()) {
            throw new IllegalStateException("Cannot modify todo in current status: " + todo.getStatus());
        }

        // Application des modifications via les méthodes métier de l'agrégat
        if (command.title() != null) {
            todo.updateTitle(command.title());
        }

        if (command.description() != null) {
            todo.updateDescription(command.description());
        }

        if (command.priority() != null) {
            todo.updatePriority(command.priority());
        }

        if (command.dueDate() != null) {
            todo.updateDueDate(command.dueDate());
        }

        Todo savedTodo = todoRepository.save(todo);

        logger.info("Todo updated successfully with id: {}", savedTodo.getId());

        return TodoResponse.from(savedTodo);
    }
}
