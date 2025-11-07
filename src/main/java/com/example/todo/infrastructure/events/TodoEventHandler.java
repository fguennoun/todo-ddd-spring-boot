package com.example.todo.infrastructure.events;

import com.example.todo.domain.events.TodoCompletedEvent;
import com.example.todo.domain.events.TodoCreatedEvent;
import com.example.todo.domain.events.TodoStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * TodoEventHandler
 *
 * Gestionnaire d'événements du domaine Todo.
 * Démontre le découplage via les événements et les traitements asynchrones.
 *
 * Concepts DDD appliqués :
 * - Event Handlers : Réaction aux événements du domaine
 * - Loose Coupling : Découplage entre agrégats
 * - Side Effects : Traitements secondaires (notifications, statistiques)
 *
 * @author Todo Team
 */
@Component
public class TodoEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(TodoEventHandler.class);

    /**
     * Traite l'événement de création d'un Todo
     *
     * @param event événement de création
     */
    @Async
    @EventListener
    public void handle(TodoCreatedEvent event) {
        logger.info("Todo created: {} for user: {} at {}",
                   event.aggregateId(), event.userId(), event.occurredOn());

        // Exemples de traitements possibles :
        // - Envoi de notification
        // - Mise à jour de statistiques utilisateur
        // - Audit log
        // - Intégration avec systèmes externes

        try {
            // Simulation d'un traitement asynchrone
            Thread.sleep(100);

            // Ici on pourrait :
            // - Incrémenter un compteur de todos créés
            // - Envoyer une notification push
            // - Logger dans un système d'audit externe

            logger.debug("Todo creation event processed successfully for: {}", event.aggregateId());

        } catch (Exception e) {
            logger.error("Error processing TodoCreatedEvent for: {}", event.aggregateId(), e);
            // En cas d'erreur, on pourrait :
            // - Republier l'événement
            // - L'envoyer dans une dead letter queue
            // - Alerter les administrateurs
        }
    }

    /**
     * Traite l'événement de complétion d'un Todo
     *
     * @param event événement de complétion
     */
    @Async
    @EventListener
    public void handle(TodoCompletedEvent event) {
        logger.info("Todo completed: {} by user: {} at {}",
                   event.aggregateId(), event.userId(), event.completedAt());

        try {
            // Exemples de traitements pour la complétion :
            // - Calcul du score de productivité
            // - Déclenchement de workflows suivants
            // - Mise à jour de tableaux de bord
            // - Notifications d'équipe

            Thread.sleep(50);

            logger.debug("Todo completion event processed for: {}", event.aggregateId());

        } catch (Exception e) {
            logger.error("Error processing TodoCompletedEvent for: {}", event.aggregateId(), e);
        }
    }

    /**
     * Traite les événements de changement de statut
     *
     * @param event événement de changement de statut
     */
    @Async
    @EventListener
    public void handle(TodoStatusChangedEvent event) {
        logger.info("Todo status changed: {} from {} to {} by user: {}",
                   event.aggregateId(), event.previousStatus(),
                   event.newStatus(), event.userId());

        try {
            // Exemples de traitements pour les changements de statut :
            // - Mise à jour des métriques temps réel
            // - Notifications conditionnelles
            // - Synchronisation avec systèmes externes

            // Logique spécifique selon le type de transition
            switch (event.newStatus()) {
                case IN_PROGRESS -> {
                    // Todo démarré - peut déclencher des timers
                    logger.debug("Todo started: {}", event.aggregateId());
                }
                case COMPLETED -> {
                    // Todo complété - déjà géré par TodoCompletedEvent
                    logger.debug("Todo completed via status change: {}", event.aggregateId());
                }
                case CANCELLED -> {
                    // Todo annulé - nettoyage éventuel
                    logger.debug("Todo cancelled: {}", event.aggregateId());
                }
            }

        } catch (Exception e) {
            logger.error("Error processing TodoStatusChangedEvent for: {}", event.aggregateId(), e);
        }
    }
}
