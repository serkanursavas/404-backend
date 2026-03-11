-- ============================================================
-- V3: Envers Audit Tables
-- These tables were already created by Hibernate (ddl-auto=update).
-- This migration is IDEMPOTENT (IF NOT EXISTS) — it is recorded here
-- for Flyway history tracking only.
-- ============================================================

-- revinfo: Envers revision metadata table
CREATE TABLE IF NOT EXISTS revinfo (
    id          INTEGER      NOT NULL,
    client_ip   VARCHAR(255),
    request_url VARCHAR(255),
    squad_id    INTEGER,
    timestamp   BIGINT       NOT NULL,
    user_id     INTEGER,
    username    VARCHAR(255),
    CONSTRAINT revinfo_pkey PRIMARY KEY (id)
);

-- revinfo sequence
CREATE SEQUENCE IF NOT EXISTS revinfo_id_seq;

-- user_log_table
CREATE TABLE IF NOT EXISTS user_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    email               VARCHAR(255),
    is_super_admin      BOOLEAN,
    password            VARCHAR(255),
    role                VARCHAR(255),
    username            VARCHAR(255),
    CONSTRAINT user_log_table_pkey PRIMARY KEY (id, rev)
);

-- squad_log_table
CREATE TABLE IF NOT EXISTS squad_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    invite_code         VARCHAR(255),
    name                VARCHAR(255),
    owner_user_id       INTEGER,
    CONSTRAINT squad_log_table_pkey PRIMARY KEY (id, rev)
);

-- player_log_table
CREATE TABLE IF NOT EXISTS player_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    name                VARCHAR(255),
    position            VARCHAR(255),
    squad_id            INTEGER,
    CONSTRAINT player_log_table_pkey PRIMARY KEY (id, rev)
);

-- game_log_table
CREATE TABLE IF NOT EXISTS game_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    away_team_score     INTEGER,
    date_time           TIMESTAMP(6),
    home_team_score     INTEGER,
    is_played           BOOLEAN,
    is_voted            BOOLEAN,
    location            VARCHAR(255),
    mvp_id              INTEGER,
    weather             VARCHAR(255),
    game_location_id    INTEGER,
    squad_id            INTEGER,
    CONSTRAINT game_log_table_pkey PRIMARY KEY (id, rev)
);

-- game_location_log_table
CREATE TABLE IF NOT EXISTS game_location_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    address             VARCHAR(255),
    name                VARCHAR(255),
    squad_id            INTEGER,
    CONSTRAINT game_location_log_table_pkey PRIMARY KEY (id, rev)
);

-- roster_log_table
CREATE TABLE IF NOT EXISTS roster_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    game_id             INTEGER,
    squad_id            INTEGER,
    CONSTRAINT roster_log_table_pkey PRIMARY KEY (id, rev)
);

-- goal_log_table
CREATE TABLE IF NOT EXISTS goal_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    game_id             INTEGER,
    player_id           INTEGER,
    squad_id            INTEGER,
    CONSTRAINT goal_log_table_pkey PRIMARY KEY (id, rev)
);

-- rating_log_table
CREATE TABLE IF NOT EXISTS rating_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    game_id             INTEGER,
    player_id           INTEGER,
    rating_value        INTEGER,
    squad_id            INTEGER,
    voter_id            INTEGER,
    CONSTRAINT rating_log_table_pkey PRIMARY KEY (id, rev)
);

-- persona_log_table
CREATE TABLE IF NOT EXISTS persona_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    description         VARCHAR(255),
    emoji               VARCHAR(255),
    name                VARCHAR(255),
    squad_id            INTEGER,
    CONSTRAINT persona_log_table_pkey PRIMARY KEY (id, rev)
);

-- player_persona_log_table
CREATE TABLE IF NOT EXISTS player_persona_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    persona_id          INTEGER,
    player_id           INTEGER,
    squad_id            INTEGER,
    CONSTRAINT player_persona_log_table_pkey PRIMARY KEY (id, rev)
);

-- roster_persona_log_table
CREATE TABLE IF NOT EXISTS roster_persona_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    persona_id          INTEGER,
    roster_id           INTEGER,
    squad_id            INTEGER,
    CONSTRAINT roster_persona_log_table_pkey PRIMARY KEY (id, rev)
);

-- join_request_log_table
CREATE TABLE IF NOT EXISTS join_request_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    squad_id            INTEGER,
    status              VARCHAR(255),
    user_id             INTEGER,
    CONSTRAINT join_request_log_table_pkey PRIMARY KEY (id, rev)
);

-- squad_request_log_table
CREATE TABLE IF NOT EXISTS squad_request_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    status              VARCHAR(255),
    user_id             INTEGER,
    CONSTRAINT squad_request_log_table_pkey PRIMARY KEY (id, rev)
);

-- group_membership_log_table
CREATE TABLE IF NOT EXISTS group_membership_log_table (
    id                  INTEGER NOT NULL,
    rev                 INTEGER NOT NULL,
    revtype             SMALLINT,
    revend              INTEGER,
    active              BOOLEAN,
    created_at          TIMESTAMP(6),
    updated_at          TIMESTAMP(6),
    created_by_user_id  INTEGER,
    updated_by_user_id  INTEGER,
    joined_at           TIMESTAMP(6),
    player_id           INTEGER,
    role                VARCHAR(255),
    squad_id            INTEGER,
    user_id             INTEGER,
    CONSTRAINT group_membership_log_table_pkey PRIMARY KEY (id, rev)
);

-- action_type generated columns (added by AuditTableInitializer)
-- These are GENERATED ALWAYS AS columns — cannot be added via IF NOT EXISTS.
-- AuditTableInitializer handles these at startup; no-op if already present.
