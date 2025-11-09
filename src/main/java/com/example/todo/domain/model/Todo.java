package com.example.todo.domain.model;

import com.example.todo.domain.events.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Todo Aggregate Root
 *
 * Agrégat principal du domaine Todo.
 * Encapsule toute la logique métier et maintient la cohérence des invariants.
 *
 * Concepts DDD appliqués :
 * - Aggregate Root : Point d'entrée unique pour les modifications
 * - Domain Events : Émission d'événements lors des changements d'état
 * - Business Invariants : Validation des règles métier
 * - Encapsulation : Logique métier encapsulée dans l'agrégat
 *
 * Règles métier :
 * - Un Todo doit avoir un titre non vide
 * - Les transitions de statut doivent respecter les règles définies
 * - Un Todo complété ne peut plus être modifié
 * - La date d'échéance ne peut pas être dans le passé (sauf pour les todos existants)
 *
 * @author Todo Team
 */
public class Todo {

    private final TodoId id;
    private String title;
    private String description;
    private TodoStatus status;
    private TodoPriority priority;
    private Instant dueDate;
    private final String userId;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    // Domain Events - Pattern pour publier des événements
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Constructeur pour la création d'un nouveau Todo
     */
    private Todo(TodoId id, String title, String description, TodoPriority priority,
                Instant dueDate, String userId) {
        this.id = Objects.requireNonNull(id, "Id cannot be null");
        this.userId = Objects.requireNonNull(userId, "UserId cannot be null");
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.status = TodoStatus.PENDING;

        // Validation and assignment without touching updatedAt so creation keeps createdAt == updatedAt
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title cannot be null or empty");
        }
        if (title.trim().length() > 255) {
            throw new IllegalArgumentException("title cannot exceed 255 characters");
        }
        this.title = title.trim();

        this.description = description != null ? description.trim() : null;

        this.priority = Objects.requireNonNull(priority, "Priority cannot be null");

        // Validate due date for creation: cannot be in the past
        if (dueDate != null && dueDate.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }
        this.dueDate = dueDate;

        // Émission de l'événement de création
        addDomainEvent(new TodoCreatedEvent(
            this.id, this.title, this.description, this.priority,
            this.dueDate, this.userId, this.createdAt
        ));
    }

    /**
     * Constructeur pour la reconstruction depuis la persistence
     */
    public Todo(TodoId id, String title, String description, TodoStatus status,
                TodoPriority priority, Instant dueDate, String userId,
                Instant createdAt, Instant updatedAt, Instant completedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    /**
     * Factory method pour créer un nouveau Todo
     *
     * @param title titre du todo
     * @param description description du todo
     * @param priority priorité du todo
     * @param dueDate date d'échéance (optionnelle)
     * @param userId identifiant de l'utilisateur
     * @return nouveau Todo
     */
    public static Todo create(String title, String description, TodoPriority priority,
                             Instant dueDate, String userId) {
        return new Todo(TodoId.generate(), title, description, priority, dueDate, userId);
    }

    /**
     * Met à jour le titre du Todo
     *
     * @param newTitle nouveau titre
     */
    public void updateTitle(String newTitle) {
        validateNotCompleted("Cannot update title of a completed todo");

        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("title cannot be null or empty");
        }

        if (newTitle.trim().length() > 255) {
            throw new IllegalArgumentException("title cannot exceed 255 characters");
        }

        this.title = newTitle.trim();
        this.updatedAt = nowAfter(this.updatedAt);
    }

    /**
     * Met à jour la description du Todo
     *
     * @param newDescription nouvelle description
     */
    public void updateDescription(String newDescription) {
        validateNotCompleted("Cannot update description of a completed todo");

        this.description = newDescription != null ? newDescription.trim() : null;
        this.updatedAt = nowAfter(this.updatedAt);
    }

    /**
     * Met à jour la priorité du Todo
     *
     * @param newPriority nouvelle priorité
     */
    public void updatePriority(TodoPriority newPriority) {
        validateNotCompleted("Cannot update priority of a completed todo");

        this.priority = Objects.requireNonNull(newPriority, "Priority cannot be null");
        this.updatedAt = nowAfter(this.updatedAt);
    }

    /**
     * Met à jour la date d'échéance du Todo
     *
     * @param newDueDate nouvelle date d'échéance
     */
    public void updateDueDate(Instant newDueDate) {
        validateNotCompleted("Cannot update due date of a completed todo");

        // Validation : la date ne peut pas être dans le passé pour un nouveau todo
        if (newDueDate != null && newDueDate.isBefore(Instant.now()) && this.createdAt.equals(this.updatedAt)) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }

        this.dueDate = newDueDate;
        this.updatedAt = nowAfter(this.updatedAt);
    }

    /**
     * Démarre le Todo (passage en IN_PROGRESS)
     */
    public void start() {
        changeStatus(TodoStatus.IN_PROGRESS);
    }

    /**
     * Complète le Todo
     */
    public void complete() {
        changeStatus(TodoStatus.COMPLETED);
        this.completedAt = Instant.now();

        // Émission de l'événement de complétion
        addDomainEvent(new TodoCompletedEvent(
            this.id, this.userId, this.completedAt, Instant.now()
        ));
    }

    /**
     * Annule le Todo
     */
    public void cancel() {
        changeStatus(TodoStatus.CANCELLED);
    }

    /**
     * Change le statut du Todo avec validation des transitions
     *
     * @param newStatus nouveau statut
     */
    private void changeStatus(TodoStatus newStatus) {
        Objects.requireNonNull(newStatus, "Status cannot be null");

        if (this.status == newStatus) {
            return; // Pas de changement
        }

        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", this.status, newStatus)
            );
        }

    TodoStatus previousStatus = this.status;
    this.status = newStatus;
    this.updatedAt = nowAfter(this.updatedAt);

        // Émission de l'événement de changement de statut
        addDomainEvent(new TodoStatusChangedEvent(
            this.id, previousStatus, newStatus, this.userId, Instant.now()
        ));
    }

    /**
     * Return an Instant guaranteed to be strictly after the provided previous instant.
     */
    private Instant nowAfter(Instant previous) {
        Instant now = Instant.now();
        if (previous == null) return now;
        return now.isAfter(previous) ? now : previous.plusNanos(1);
    }

    /**
     * Vérifie si le Todo est en retard
     *
     * @return true si en retard
     */
    public boolean isOverdue() {
        return dueDate != null &&
               Instant.now().isAfter(dueDate) &&
               !status.isFinal();
    }

    /**
     * Vérifie si le Todo peut être modifié
     *
     * @return true si modifiable
     */
    public boolean isEditable() {
        return status.isEditable();
    }

    /**
     * Validation que le Todo n'est pas complété
     */
    private void validateNotCompleted(String message) {
        if (status == TodoStatus.COMPLETED) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Ajoute un événement du domaine
     */
    private void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * Récupère et vide la liste des événements du domaine
     *
     * @return liste des événements
     */
    public List<DomainEvent> getAndClearDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    // Getters
    public TodoId getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TodoStatus getStatus() { return status; }
    public TodoPriority getPriority() { return priority; }
    public Instant getDueDate() { return dueDate; }
    public String getUserId() { return userId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getCompletedAt() { return completedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Todo todo = (Todo) o;
        return Objects.equals(id, todo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Todo{id=%s, title='%s', status=%s, priority=%s}",
                           id, title, status, priority);
    }
}
