package com.example.todo.infrastructure.persistence;

import com.example.todo.domain.model.TodoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

/**
 * TodoJpaRepository
 *
 * Repository Spring Data JPA pour l'accès aux données des Todos.
 * Définit les requêtes spécifiques nécessaires à l'application.
 *
 * @author Todo Team
 */
@Repository
public interface TodoJpaRepository extends JpaRepository<TodoJpaEntity, String> {

    /**
     * Trouve tous les Todos d'un utilisateur avec pagination
     */
    Page<TodoJpaEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Trouve les Todos d'un utilisateur par statut
     */
    Page<TodoJpaEntity> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, TodoStatus status, Pageable pageable);

    /**
     * Trouve les Todos en retard pour un utilisateur
     */
    @Query("SELECT t FROM TodoJpaEntity t WHERE t.userId = :userId " +
           "AND t.dueDate < :currentTime " +
           "AND t.status IN ('PENDING', 'IN_PROGRESS')")
    List<TodoJpaEntity> findOverdueTodosByUserId(@Param("userId") String userId,
                                                 @Param("currentTime") Instant currentTime);

    /**
     * Trouve les Todos avec date d'échéance dans une période
     */
    List<TodoJpaEntity> findByUserIdAndDueDateBetweenOrderByDueDateAsc(String userId, Instant from, Instant to);

    /**
     * Compte les Todos par utilisateur et statut
     */
    long countByUserIdAndStatus(String userId, TodoStatus status);

    /**
     * Supprime tous les Todos d'un utilisateur
     */
    void deleteByUserId(String userId);

    /**
     * Trouve les Todos par titre (recherche partielle)
     */
    @Query("SELECT t FROM TodoJpaEntity t WHERE t.userId = :userId " +
           "AND LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "ORDER BY t.createdAt DESC")
    Page<TodoJpaEntity> findByUserIdAndTitleContainingIgnoreCase(@Param("userId") String userId,
                                                                 @Param("title") String title,
                                                                 Pageable pageable);

    /**
     * Trouve les Todos par priorité
     */
    Page<TodoJpaEntity> findByUserIdAndPriorityLevelOrderByCreatedAtDesc(String userId, int priorityLevel, Pageable pageable);

    /**
     * Statistiques : nombre de Todos complétés par période
     */
    @Query("SELECT COUNT(t) FROM TodoJpaEntity t WHERE t.userId = :userId " +
           "AND t.status = 'COMPLETED' " +
           "AND t.completedAt BETWEEN :from AND :to")
    long countCompletedTodosByUserAndPeriod(@Param("userId") String userId,
                                           @Param("from") Instant from,
                                           @Param("to") Instant to);
}
