package com.example.todo.infrastructure.persistence;

import com.example.todo.domain.events.DomainEvent;
import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.TodoStatus;
import com.example.todo.domain.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * TodoRepositoryImpl
 *
 * Implémentation concrète du repository du domaine.
 * Adapte Spring Data JPA aux besoins du domaine et gère la publication des événements.
 *
 * Concepts DDD appliqués :
 * - Repository Implementation : Implémentation dans la couche infrastructure
 * - Domain Events Publishing : Publication automatique des événements
 * - Adapter Pattern : Adaptation entre les interfaces
 *
 * @author Todo Team
 */
@Repository
public class TodoRepositoryImpl implements TodoRepository {

    private static final Logger logger = LoggerFactory.getLogger(TodoRepositoryImpl.class);

    private final TodoJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TodoRepositoryImpl(TodoJpaRepository jpaRepository, ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Todo save(Todo todo) {
        logger.debug("Saving todo with id: {}", todo.getId());

        // Conversion vers l'entité JPA
        TodoJpaEntity entity = TodoJpaEntity.fromDomain(todo);

        // Sauvegarde
        TodoJpaEntity savedEntity = jpaRepository.save(entity);

        // Conversion vers le domaine
        Todo savedTodo = savedEntity.toDomain();

        // Publication des événements du domaine
        publishDomainEvents(todo);

        logger.debug("Todo saved successfully with id: {}", savedTodo.getId());

        return savedTodo;
    }

    @Override
    public Optional<Todo> findById(TodoId id) {
        logger.debug("Finding todo by id: {}", id);

        return jpaRepository.findById(id.value())
            .map(TodoJpaEntity::toDomain);
    }

    @Override
    public Page<Todo> findByUserId(String userId, Pageable pageable) {
        logger.debug("Finding todos by userId: {} with pageable: {}", userId, pageable);

        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(TodoJpaEntity::toDomain);
    }

    @Override
    public Page<Todo> findByUserIdAndStatus(String userId, TodoStatus status, Pageable pageable) {
        logger.debug("Finding todos by userId: {} and status: {} with pageable: {}", userId, status, pageable);

        return jpaRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable)
            .map(TodoJpaEntity::toDomain);
    }

    @Override
    public List<Todo> findOverdueTodosByUserId(String userId, Instant currentTime) {
        logger.debug("Finding overdue todos for userId: {} at time: {}", userId, currentTime);

        return jpaRepository.findOverdueTodosByUserId(userId, currentTime)
            .stream()
            .map(TodoJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Todo> findByUserIdAndDueDateBetween(String userId, Instant from, Instant to) {
        logger.debug("Finding todos by userId: {} between {} and {}", userId, from, to);

        return jpaRepository.findByUserIdAndDueDateBetweenOrderByDueDateAsc(userId, from, to)
            .stream()
            .map(TodoJpaEntity::toDomain)
            .toList();
    }

    @Override
    public long countByUserIdAndStatus(String userId, TodoStatus status) {
        logger.debug("Counting todos by userId: {} and status: {}", userId, status);

        return jpaRepository.countByUserIdAndStatus(userId, status);
    }

    @Override
    public boolean existsById(TodoId id) {
        logger.debug("Checking existence of todo with id: {}", id);

        return jpaRepository.existsById(id.value());
    }

    @Override
    public void deleteById(TodoId id) {
        logger.debug("Deleting todo with id: {}", id);

        jpaRepository.deleteById(id.value());
    }

    @Override
    public void deleteByUserId(String userId) {
        logger.debug("Deleting all todos for userId: {}", userId);

        jpaRepository.deleteByUserId(userId);
    }

    /**
     * Publie les événements du domaine via Spring's ApplicationEventPublisher
     *
     * @param todo agrégat contenant les événements
     */
    private void publishDomainEvents(Todo todo) {
        List<DomainEvent> events = todo.getAndClearDomainEvents();

        for (DomainEvent event : events) {
            logger.debug("Publishing domain event: {} for aggregate: {}",
                        event.getEventType(), event.getAggregateId());

            eventPublisher.publishEvent(event);
        }
    }
}
