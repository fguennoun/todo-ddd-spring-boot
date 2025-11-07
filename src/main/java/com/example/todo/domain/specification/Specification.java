package com.example.todo.domain.specification;

import com.example.todo.domain.model.Todo;

/**
 * Specification Interface
 *
 * Pattern Specification pour encapsuler les critères de sélection métier.
 * Permet de composer des règles métier complexes de manière lisible.
 *
 * Concepts DDD appliqués :
 * - Specification Pattern : Encapsulation des critères de sélection
 * - Composable Rules : Composition de règles métier
 * - Domain Language : Expression des règles en langage métier
 *
 * @author Todo Team
 */
@FunctionalInterface
public interface Specification<T> {

    /**
     * Évalue si l'entité satisfait la spécification
     *
     * @param candidate entité à évaluer
     * @return true si la spécification est satisfaite
     */
    boolean isSatisfiedBy(T candidate);

    /**
     * Combine cette spécification avec une autre via AND logique
     *
     * @param other autre spécification
     * @return nouvelle spécification combinée
     */
    default Specification<T> and(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }

    /**
     * Combine cette spécification avec une autre via OR logique
     *
     * @param other autre spécification
     * @return nouvelle spécification combinée
     */
    default Specification<T> or(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    /**
     * Négation de cette spécification
     *
     * @return spécification négée
     */
    default Specification<T> not() {
        return candidate -> !this.isSatisfiedBy(candidate);
    }
}
