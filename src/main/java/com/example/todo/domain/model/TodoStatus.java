package com.example.todo.domain.model;

/**
 * TodoStatus Value Object
 *
 * Énumération représentant les différents états d'un Todo.
 * Implémente le pattern Value Object pour encapsuler la logique métier
 * des transitions d'état.
 *
 * Concepts DDD appliqués :
 * - Value Object : Encapsule la logique métier des statuts
 * - Domain Language : États explicites du domaine métier
 * - Business Rules : Règles de transition encapsulées
 *
 * États possibles :
 * - PENDING : Todo créé, en attente de traitement
 * - IN_PROGRESS : Todo en cours de réalisation
 * - COMPLETED : Todo terminé
 * - CANCELLED : Todo annulé
 *
 * @author Todo Team
 */
public enum TodoStatus {

    PENDING("En attente", "Todo créé, en attente de traitement"),
    IN_PROGRESS("En cours", "Todo en cours de réalisation"),
    COMPLETED("Terminé", "Todo terminé avec succès"),
    CANCELLED("Annulé", "Todo annulé, ne sera pas réalisé");

    private final String displayName;
    private final String description;

    TodoStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Vérifie si la transition vers un nouveau statut est autorisée
     *
     * Règles métier :
     * - PENDING peut aller vers IN_PROGRESS ou CANCELLED
     * - IN_PROGRESS peut aller vers COMPLETED ou CANCELLED
     * - COMPLETED et CANCELLED sont des états finaux
     *
     * @param newStatus nouveau statut souhaité
     * @return true si la transition est autorisée
     */
    public boolean canTransitionTo(TodoStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == IN_PROGRESS || newStatus == CANCELLED || newStatus == COMPLETED;
            case IN_PROGRESS -> newStatus == COMPLETED || newStatus == CANCELLED;
            case COMPLETED, CANCELLED -> false; // États finaux
        };
    }

    /**
     * Vérifie si le statut est un état final
     *
     * @return true si c'est un état final
     */
    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED;
    }

    /**
     * Vérifie si le Todo peut être modifié dans ce statut
     *
     * @return true si le Todo peut être modifié
     */
    public boolean isEditable() {
        return this == PENDING || this == IN_PROGRESS;
    }
}
