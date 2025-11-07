package com.example.todo.application.usecase;

import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DeleteTodoUseCase
 *
 * Use Case pour supprimer un Todo.
 *
 * @author Todo Team
 */
@Service
@Transactional
public class DeleteTodoUseCase {

    private static final Logger logger = LoggerFactory.getLogger(DeleteTodoUseCase.class);

    private final TodoRepository todoRepository;

    public DeleteTodoUseCase(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Supprime un Todo
     *
     * @param todoId identifiant du Todo à supprimer
     * @param userId identifiant de l'utilisateur
     * @throws TodoNotFoundException si le Todo n'existe pas
     */
    @CacheEvict(value = {"todos", "todoLists", "todoListsByStatus"}, allEntries = true)
    public void execute(String todoId, String userId) {
        logger.info("Deleting todo with id: {} for user: {}", todoId, userId);

        TodoId id = TodoId.of(todoId);

        // Vérification de l'existence et de la propriété
        todoRepository.findById(id)
            .filter(todo -> todo.getUserId().equals(userId))
            .orElseThrow(() -> new TodoNotFoundException("Todo not found with id: " + todoId));

        todoRepository.deleteById(id);

        logger.info("Todo deleted successfully with id: {}", todoId);
    }
}
