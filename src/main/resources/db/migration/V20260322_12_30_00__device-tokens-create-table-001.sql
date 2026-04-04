CREATE TABLE IF NOT EXISTS device_tokens (
    id                  SERIAL PRIMARY KEY,
    user_id             INTEGER NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    token               VARCHAR(255) NOT NULL,
    platform            VARCHAR(10) NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER REFERENCES "user"(id),
    updated_by_user_id  INTEGER REFERENCES "user"(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_device_tokens_token ON device_tokens(token);
CREATE INDEX IF NOT EXISTS idx_device_tokens_user_id ON device_tokens(user_id);

-- Envers audit table (rev kolonu plain INTEGER — FK yok, diğer log_table'larla aynı pattern)
CREATE TABLE IF NOT EXISTS device_tokens_log_table (
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
    platform            VARCHAR(10),
    CONSTRAINT device_tokens_log_table_pkey PRIMARY KEY (id, rev)
);
