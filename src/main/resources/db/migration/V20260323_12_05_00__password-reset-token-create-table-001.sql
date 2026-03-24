CREATE TABLE IF NOT EXISTS password_reset_token (
    id                  SERIAL PRIMARY KEY,
    user_id             INTEGER NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    token               VARCHAR(255) NOT NULL,
    expires_at          TIMESTAMP(6) NOT NULL,
    used                BOOLEAN NOT NULL DEFAULT FALSE,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER REFERENCES "user"(id),
    updated_by_user_id  INTEGER REFERENCES "user"(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_password_reset_token_token ON password_reset_token(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_token_user_id ON password_reset_token(user_id);

-- Envers audit table
CREATE TABLE IF NOT EXISTS password_reset_token_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    user_id             INTEGER,
    token               VARCHAR(255),
    expires_at          TIMESTAMP(6),
    used                BOOLEAN,
    CONSTRAINT password_reset_token_log_table_pkey PRIMARY KEY (id, rev)
);
