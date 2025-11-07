package com.example.todo.infrastructure.persistence;

import com.example.todo.domain.model.Todo;

/**
 * TodoMapper
 *
 * Classe utilitaire pour la conversion entre les entités du domaine et de persistence.
 * Utilise les méthodes de conversion intégrées dans TodoJpaEntity.
 *
 * @author Todo Team
 */
public class TodoMapper {
    private TodoMapper() {
        // Empêche l'instanciation de cette classe utilitaire
    }

    /**
     * Convertit un Todo du domaine vers une entité JPA
     *
     * @param todo Todo du domaine
     * @return entité JPA
     */
    public static TodoJpaEntity toJpaEntity(Todo todo) {
        return TodoJpaEntity.fromDomain(todo);
    }

    /**
     * Convertit une entité JPA vers un Todo du domaine
     *
     * @param entity entité JPA
     * @return Todo du domaine
     */
    public static Todo toDomainEntity(TodoJpaEntity entity) {
        return entity.toDomain();
    }
}
