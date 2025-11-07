package com.example.todo.application.usecase;

import com.example.todo.application.dto.TodoResponse;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * GetTodoUseCase
 *
 * Use Case pour récupérer un Todo par son identifiant.
 * Inclut la mise en cache pour optimiser les performances.
 *
 * @author Todo Team
 */
@Service
@Transactional(readOnly = true)
public class GetTodoUseCase {

    private static final Logger logger = LoggerFactory.getLogger(GetTodoUseCase.class);

    private final TodoRepository todoRepository;

    public GetTodoUseCase(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Récupère un Todo par son identifiant
     *
     * @param todoId identifiant du Todo
     * @param userId identifiant de l'utilisateur (pour vérification de propriété)
     * @return Todo trouvé
     * @throws TodoNotFoundException si le Todo n'existe pas
     * @throws UnauthorizedAccessException si l'utilisateur n'est pas propriétaire
     */
    @Cacheable(value = "todos", key = "#todoId")
    public TodoResponse execute(String todoId, String userId) {
        logger.debug("Retrieving todo with id: {} for user: {}", todoId, userId);

        TodoId id = TodoId.of(todoId);

        return todoRepository.findById(id)
            .filter(todo -> todo.getUserId().equals(userId)) // Vérification de propriété
            .map(TodoResponse::from)
            .orElseThrow(() -> {
                logger.warn("Todo not found or access denied - id: {}, user: {}", todoId, userId);
                return new TodoNotFoundException("Todo not found with id: " + todoId);
            });
    }
}
