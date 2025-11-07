package com.example.todo.domain.service;

import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoStatus;
import com.example.todo.domain.repository.TodoRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * TodoDomainService
 *
 * Service du domaine qui encapsule la logique métier complexe
 * qui ne peut pas être attribuée à un seul agrégat.
 *
 * Concepts DDD appliqués :
 * - Domain Service : Logique métier qui ne appartient pas à un agrégat spécifique
 * - Stateless : Service sans état, opérations pures
 * - Domain Logic : Logique métier pure, sans dépendances techniques
 *
 * @author Todo Team
 */
public class TodoDomainService {

    private final TodoRepository todoRepository;

    public TodoDomainService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Calcule le score de productivité d'un utilisateur
     * basé sur ses Todos complétés dans les dernières semaines
     *
     * @param userId identifiant de l'utilisateur
     * @param weeks nombre de semaines à considérer
     * @return score de productivité (0-100)
     */
    public int calculateProductivityScore(String userId, int weeks) {
        Instant fromDate = Instant.now().minus(weeks * 7L, ChronoUnit.DAYS);
        Instant toDate = Instant.now();

        List<Todo> recentTodos = todoRepository.findByUserIdAndDueDateBetween(userId, fromDate, toDate);

        if (recentTodos.isEmpty()) {
            return 0;
        }

        long completedCount = recentTodos.stream()
            .mapToLong(todo -> todo.getStatus() == TodoStatus.COMPLETED ? 1L : 0L)
            .sum();

        long totalCount = recentTodos.size();

        return (int) ((completedCount * 100) / totalCount);
    }

    /**
     * Détermine si un utilisateur a une charge de travail élevée
     *
     * @param userId identifiant de l'utilisateur
     * @return true si la charge est élevée
     */
    public boolean hasHighWorkload(String userId) {
        long pendingCount = todoRepository.countByUserIdAndStatus(userId, TodoStatus.PENDING);
        long inProgressCount = todoRepository.countByUserIdAndStatus(userId, TodoStatus.IN_PROGRESS);

        return (pendingCount + inProgressCount) > 10; // Seuil métier
    }

    /**
     * Trouve les Todos critiques en retard pour un utilisateur
     *
     * @param userId identifiant de l'utilisateur
     * @return liste des Todos critiques en retard
     */
    public List<Todo> findCriticalOverdueTodos(String userId) {
        return todoRepository.findOverdueTodosByUserId(userId, Instant.now())
            .stream()
            .filter(todo -> todo.getPriority().isCritical())
            .toList();
    }

    /**
     * Vérifie si un utilisateur peut créer de nouveaux Todos
     * (règle métier : limite de Todos actifs)
     *
     * @param userId identifiant de l'utilisateur
     * @return true si l'utilisateur peut créer de nouveaux Todos
     */
    public boolean canCreateNewTodo(String userId) {
        long activeCount = todoRepository.countByUserIdAndStatus(userId, TodoStatus.PENDING) +
                          todoRepository.countByUserIdAndStatus(userId, TodoStatus.IN_PROGRESS);

        return activeCount < 50; // Limite métier
    }
}
