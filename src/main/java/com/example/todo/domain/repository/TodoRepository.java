package com.example.todo.domain.repository;

import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.TodoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * TodoRepository Interface
 *
 * Interface du repository définie dans la couche domaine.
 * Suit le principe d'inversion des dépendances (DIP) -
 * le domaine définit le contrat, l'infrastructure l'implémente.
 *
 * Concepts DDD appliqués :
 * - Repository Pattern : Abstraction pour l'accès aux données
 * - Domain Layer : Interface dans le domaine, implémentation dans l'infrastructure
 * - Collection-Like Interface : Interface similaire à une collection
 * - Aggregate Root Access : Accès uniquement aux aggregate roots
 *
 * @author Todo Team
 */
public interface TodoRepository {

    /**
     * Sauvegarde un Todo (création ou mise à jour)
     *
     * @param todo Todo à sauvegarder
     * @return Todo sauvegardé
     */
    Todo save(Todo todo);

    /**
     * Trouve un Todo par son identifiant
     *
     * @param id identifiant du Todo
     * @return Optional contenant le Todo s'il existe
     */
    Optional<Todo> findById(TodoId id);

    /**
     * Trouve tous les Todos d'un utilisateur
     *
     * @param userId identifiant de l'utilisateur
     * @param pageable paramètres de pagination
     * @return page de Todos
     */
    Page<Todo> findByUserId(String userId, Pageable pageable);

    /**
     * Trouve les Todos d'un utilisateur par statut
     *
     * @param userId identifiant de l'utilisateur
     * @param status statut recherché
     * @param pageable paramètres de pagination
     * @return page de Todos
     */
    Page<Todo> findByUserIdAndStatus(String userId, TodoStatus status, Pageable pageable);

    /**
     * Trouve les Todos d'un utilisateur en retard
     *
     * @param userId identifiant de l'utilisateur
     * @param currentTime instant courant
     * @return liste des Todos en retard
     */
    List<Todo> findOverdueTodosByUserId(String userId, Instant currentTime);

    /**
     * Trouve les Todos avec une date d'échéance dans une période
     *
     * @param userId identifiant de l'utilisateur
     * @param from date de début
     * @param to date de fin
     * @return liste des Todos
     */
    List<Todo> findByUserIdAndDueDateBetween(String userId, Instant from, Instant to);

    /**
     * Compte le nombre de Todos par statut pour un utilisateur
     *
     * @param userId identifiant de l'utilisateur
     * @param status statut à compter
     * @return nombre de Todos
     */
    long countByUserIdAndStatus(String userId, TodoStatus status);

    /**
     * Vérifie l'existence d'un Todo
     *
     * @param id identifiant du Todo
     * @return true si le Todo existe
     */
    boolean existsById(TodoId id);

    /**
     * Supprime un Todo
     *
     * @param id identifiant du Todo à supprimer
     */
    void deleteById(TodoId id);

    /**
     * Supprime tous les Todos d'un utilisateur
     *
     * @param userId identifiant de l'utilisateur
     */
    void deleteByUserId(String userId);
}
