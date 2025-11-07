package com.example.todo.application.usecase;

import com.example.todo.application.dto.CreateTodoCommand;
import com.example.todo.application.dto.TodoResponse;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.repository.TodoRepository;
import com.example.todo.domain.service.TodoDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CreateTodoUseCase
 *
 * Use Case pour la création d'un nouveau Todo.
 * Orchestre la logique applicative et coordonne les interactions
 * entre les différents éléments du domaine.
 *
 * Concepts DDD appliqués :
 * - Application Service : Orchestration des opérations métier
 * - Use Case : Cas d'usage métier spécifique
 * - Transaction Boundary : Gestion des transactions
 * - Domain Logic Orchestration : Coordination des services du domaine
 *
 * @author Todo Team
 */
@Service
@Transactional
public class CreateTodoUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CreateTodoUseCase.class);

    private final TodoRepository todoRepository;
    private final TodoDomainService todoDomainService;

    public CreateTodoUseCase(TodoRepository todoRepository, TodoDomainService todoDomainService) {
        this.todoRepository = todoRepository;
        this.todoDomainService = todoDomainService;
    }

    /**
     * Crée un nouveau Todo
     *
     * @param command données de création
     * @param userId identifiant de l'utilisateur
     * @return Todo créé
     * @throws IllegalStateException si l'utilisateur a atteint la limite de Todos actifs
     */
    public TodoResponse execute(CreateTodoCommand command, String userId) {
        logger.info("Creating new todo for user: {} with title: {}", userId, command.title());

        // Vérification des règles métier via le domain service
        if (!todoDomainService.canCreateNewTodo(userId)) {
            throw new IllegalStateException("User has reached the maximum number of active todos");
        }

        // Création de l'agrégat Todo via la factory method
        Todo todo = Todo.create(
            command.title(),
            command.description(),
            command.priority(),
            command.dueDate(),
            userId
        );

        // Sauvegarde (avec publication automatique des événements du domaine)
        Todo savedTodo = todoRepository.save(todo);

        logger.info("Todo created successfully with id: {}", savedTodo.getId());

        return TodoResponse.from(savedTodo);
    }
}
