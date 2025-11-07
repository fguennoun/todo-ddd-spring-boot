package com.example.todo.domain.events;

import com.example.todo.domain.model.TodoId;
import java.time.Instant;

/**
 * DomainEvent Interface
 *
 * Interface de base pour tous les événements du domaine.
 * Les événements représentent des faits métier importants qui se sont produits.
 *
 * Concepts DDD appliqués :
 * - Domain Events : Capture les événements métier significatifs
 * - Event Sourcing : Traçabilité des changements d'état
 * - Loose Coupling : Découplage entre aggregates
 *
 * @author Todo Team
 */
public interface DomainEvent {

    /**
     * Identifiant de l'agrégat concerné par l'événement
     *
     * @return identifiant de l'agrégat
     */
    TodoId getAggregateId();

    /**
     * Timestamp de l'occurrence de l'événement
     *
     * @return instant de l'événement
     */
    Instant getOccurredOn();

    /**
     * Version de l'événement (pour l'évolution du schéma)
     *
     * @return version de l'événement
     */
    default int getVersion() {
        return 1;
    }

    /**
     * Type de l'événement (nom de la classe par défaut)
     *
     * @return type de l'événement
     */
    default String getEventType() {
        return this.getClass().getSimpleName();
    }
}
