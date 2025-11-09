package com.example.todo.application.usecase;

import com.example.todo.application.dto.TodoResponse;
import com.example.todo.domain.model.TodoStatus;
import com.example.todo.domain.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import java.util.stream.Collectors;
import com.example.todo.domain.model.PageRequest;
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
    public com.example.todo.domain.model.PageResult<TodoResponse> execute(String userId, Pageable pageable) {
        logger.debug("Listing todos for user: {} with pagination: {}", userId, pageable);

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        com.example.todo.domain.model.PageResult<com.example.todo.domain.model.Todo> domainPage = todoRepository.findByUserId(userId, pageRequest);

        var content = domainPage.getContent().stream()
            .map(TodoResponse::from)
            .collect(Collectors.toList());

        return new com.example.todo.domain.model.PageResult<>(
            content,
            domainPage.getPageNumber(),
            domainPage.getPageSize(),
            domainPage.getTotalElements()
        );
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
    public com.example.todo.domain.model.PageResult<TodoResponse> executeByStatus(String userId, TodoStatus status, Pageable pageable) {
        logger.debug("Listing todos for user: {} with status: {} and pagination: {}", userId, status, pageable);

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        com.example.todo.domain.model.PageResult<com.example.todo.domain.model.Todo> domainPage = todoRepository.findByUserIdAndStatus(userId, status, pageRequest);

        var content = domainPage.getContent().stream()
            .map(TodoResponse::from)
            .collect(Collectors.toList());

        return new com.example.todo.domain.model.PageResult<>(
            content,
            domainPage.getPageNumber(),
            domainPage.getPageSize(),
            domainPage.getTotalElements()
        );
    }
}
