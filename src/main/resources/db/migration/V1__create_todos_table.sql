-- =================================================================
-- Todo DDD Application Database Migration
-- Version: V1__create_todos_table.sql
-- Description: Création de la table todos et des index nécessaires
-- =================================================================

-- Table principale todos
CREATE TABLE todos (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority_level INTEGER NOT NULL DEFAULT 2,
    priority_name VARCHAR(50) NOT NULL DEFAULT 'Normale',
    due_date TIMESTAMP WITH TIME ZONE,
    user_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,

    -- Contraintes
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_priority_level CHECK (priority_level BETWEEN 1 AND 4),
    CONSTRAINT chk_completed_at CHECK (
        (status = 'COMPLETED' AND completed_at IS NOT NULL) OR
        (status != 'COMPLETED' AND completed_at IS NULL)
    )
);

-- Index pour les requêtes fréquentes
CREATE INDEX idx_todos_user_id ON todos(user_id);
CREATE INDEX idx_todos_status ON todos(status);
CREATE INDEX idx_todos_user_status ON todos(user_id, status);
CREATE INDEX idx_todos_due_date ON todos(due_date) WHERE due_date IS NOT NULL;
CREATE INDEX idx_todos_created_at ON todos(created_at);
CREATE INDEX idx_todos_priority ON todos(priority_level);

-- Index composite pour les requêtes complexes
CREATE INDEX idx_todos_user_status_due ON todos(user_id, status, due_date) WHERE due_date IS NOT NULL;
CREATE INDEX idx_todos_overdue ON todos(user_id, due_date)
    WHERE status IN ('PENDING', 'IN_PROGRESS') AND due_date < CURRENT_TIMESTAMP;

-- Commentaires sur les colonnes
COMMENT ON TABLE todos IS 'Table principale des Todos - DDD Aggregate Root';
COMMENT ON COLUMN todos.id IS 'Identifiant unique UUID du Todo';
COMMENT ON COLUMN todos.title IS 'Titre du Todo (obligatoire)';
COMMENT ON COLUMN todos.description IS 'Description détaillée (optionnelle)';
COMMENT ON COLUMN todos.status IS 'Statut : PENDING, IN_PROGRESS, COMPLETED, CANCELLED';
COMMENT ON COLUMN todos.priority_level IS 'Niveau de priorité : 1=Basse, 2=Normale, 3=Haute, 4=Critique';
COMMENT ON COLUMN todos.priority_name IS 'Nom lisible de la priorité';
COMMENT ON COLUMN todos.due_date IS 'Date d''échéance (optionnelle)';
COMMENT ON COLUMN todos.user_id IS 'Identifiant du propriétaire';
COMMENT ON COLUMN todos.created_at IS 'Date de création (audit)';
COMMENT ON COLUMN todos.updated_at IS 'Date de dernière modification (audit)';
COMMENT ON COLUMN todos.completed_at IS 'Date de complétion (si status=COMPLETED)';

-- Trigger pour mettre à jour automatiquement updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_todos_updated_at
    BEFORE UPDATE ON todos
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
