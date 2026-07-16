CREATE TABLE IF NOT EXISTS notifications (
    id                  SERIAL PRIMARY KEY,
    user_id             INTEGER      NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    squad_id            INTEGER      NOT NULL,
    title               VARCHAR(255) NOT NULL,
    body                TEXT         NOT NULL,
    type                VARCHAR(100) NOT NULL,
    data                JSONB,
    is_read             BOOLEAN      NOT NULL DEFAULT FALSE,
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by_user_id  INTEGER      REFERENCES "user"(id),
    updated_by_user_id  INTEGER      REFERENCES "user"(id)
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread  ON notifications(user_id, is_read);

-- Envers audit table
CREATE TABLE IF NOT EXISTS notifications_log_table (
    id                  INTEGER      NOT NULL,
    rev                 INTEGER      NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    user_id             INTEGER,
    squad_id            INTEGER,
    title               VARCHAR(255),
    body                TEXT,
    type                VARCHAR(100),
    data                JSONB,
    is_read             BOOLEAN,
    CONSTRAINT notifications_log_table_pkey PRIMARY KEY (id, rev)
);
