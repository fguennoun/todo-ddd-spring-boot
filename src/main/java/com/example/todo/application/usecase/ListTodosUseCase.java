package com.example.todo.application.usecase;

import com.example.todo.application.dto.TodoResponse;
import com.example.todo.domain.model.TodoStatus;
import com.example.todo.domain.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ListTodosUseCase
 *
 * Use Case pour lister les Todos d'un utilisateur avec pagination et filtrage.
 *
 * @author Todo Team
 */
@Service
@Transactional(readOnly = true)
public class ListTodosUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ListTodosUseCase.class);

    private final TodoRepository todoRepository;

    public ListTodosUseCase(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Liste tous les Todos d'un utilisateur
     *
     * @param userId identifiant de l'utilisateur
     * @param pageable paramètres de pagination
     * @return page de Todos
     */
    @Cacheable(value = "todoLists", key = "#userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<TodoResponse> execute(String userId, Pageable pageable) {
        logger.debug("Listing todos for user: {} with pagination: {}", userId, pageable);

        return todoRepository.findByUserId(userId, pageable)
            .map(TodoResponse::from);
    }

    /**
     * Liste les Todos d'un utilisateur filtrés par statut
     *
     * @param userId identifiant de l'utilisateur
     * @param status statut à filtrer
     * @param pageable paramètres de pagination
     * @return page de Todos filtrés
     */
    @Cacheable(value = "todoListsByStatus", key = "#userId + '_' + #status + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<TodoResponse> executeByStatus(String userId, TodoStatus status, Pageable pageable) {
        logger.debug("Listing todos for user: {} with status: {} and pagination: {}", userId, status, pageable);

        return todoRepository.findByUserIdAndStatus(userId, status, pageable)
            .map(TodoResponse::from);
    }
}
