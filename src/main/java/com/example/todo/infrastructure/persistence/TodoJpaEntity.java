package com.example.todo.infrastructure.persistence;

import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.TodoPriority;
import com.example.todo.domain.model.TodoStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;

/**
 * TodoJpaEntity
 *
 * Entité JPA pour la persistence des Todos.
 * Sépare les préoccupations : cette classe s'occupe uniquement de la persistence,
 * tandis que l'agrégat Todo gère la logique métier.
 *
 * Concepts DDD appliqués :
 * - Separation of Concerns : Persistence séparée du domaine
 * - Infrastructure Layer : Détails techniques de persistence
 * - Mapping : Conversion entre domaine et infrastructure
 *
 * @author Todo Team
 */
@Entity
@Table(name = "todos")
@EntityListeners(AuditingEntityListener.class)
public class TodoJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TodoStatus status;

    @Column(name = "priority_level", nullable = false)
    private int priorityLevel;

    @Column(name = "priority_name", nullable = false, length = 50)
    private String priorityName;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    // Constructeur par défaut pour JPA
    protected TodoJpaEntity() {}

    // Constructeur pour la création
    public TodoJpaEntity(String id, String title, String description, TodoStatus status,
                        int priorityLevel, String priorityName, Instant dueDate,
                        String userId, Instant createdAt, Instant updatedAt, Instant completedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priorityLevel = priorityLevel;
        this.priorityName = priorityName;
        this.dueDate = dueDate;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    /**
     * Convertit l'entité JPA vers le domaine
     *
     * @return Todo du domaine
     */
    public Todo toDomain() {
        return new Todo(
            TodoId.of(this.id),
            this.title,
            this.description,
            this.status,
            new TodoPriority(this.priorityLevel, this.priorityName),
            this.dueDate,
            this.userId,
            this.createdAt,
            this.updatedAt,
            this.completedAt
        );
    }

    /**
     * Crée une entité JPA à partir du domaine
     *
     * @param todo Todo du domaine
     * @return entité JPA
     */
    public static TodoJpaEntity fromDomain(Todo todo) {
        return new TodoJpaEntity(
            todo.getId().value(),
            todo.getTitle(),
            todo.getDescription(),
            todo.getStatus(),
            todo.getPriority().level(),
            todo.getPriority().name(),
            todo.getDueDate(),
            todo.getUserId(),
            todo.getCreatedAt(),
            todo.getUpdatedAt(),
            todo.getCompletedAt()
        );
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TodoStatus getStatus() { return status; }
    public void setStatus(TodoStatus status) { this.status = status; }

    public int getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(int priorityLevel) { this.priorityLevel = priorityLevel; }

    public String getPriorityName() { return priorityName; }
    public void setPriorityName(String priorityName) { this.priorityName = priorityName; }

    public Instant getDueDate() { return dueDate; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
