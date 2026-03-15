-- ============================================================
-- Envers Audit Tables: Add Missing Entity-Specific Columns
-- V20260312 migration only included base entity columns for
-- most tables. This migration adds the missing entity-specific
-- columns idempotently (ADD COLUMN IF NOT EXISTS).
-- ============================================================

-- game_location_log_table
-- Entity: location (String), address (String), latitude (Double), longitude (Double), squad_id
-- Missing: location, latitude, longitude  (migration had 'name' instead of 'location')
ALTER TABLE game_location_log_table ADD COLUMN IF NOT EXISTS location  VARCHAR(255);
ALTER TABLE game_location_log_table ADD COLUMN IF NOT EXISTS latitude  DOUBLE PRECISION;
ALTER TABLE game_location_log_table ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

-- goal_log_table
-- Entity: teamColor (String), game_id, player_id
-- Missing: team_color
ALTER TABLE goal_log_table ADD COLUMN IF NOT EXISTS team_color VARCHAR(255);

-- join_request_log_table
-- Entity: squad_id, user_id, playerName, playerSurname, playerPosition, playerFoot, status, reviewedAt, reviewed_by_user_id
-- Missing: player_name, player_surname, player_position, player_foot, reviewed_at, reviewed_by_user_id
ALTER TABLE join_request_log_table ADD COLUMN IF NOT EXISTS player_name          VARCHAR(255);
ALTER TABLE join_request_log_table ADD COLUMN IF NOT EXISTS player_surname       VARCHAR(255);
ALTER TABLE join_request_log_table ADD COLUMN IF NOT EXISTS player_position      VARCHAR(255);
ALTER TABLE join_request_log_table ADD COLUMN IF NOT EXISTS player_foot          VARCHAR(255);
ALTER TABLE join_request_log_table ADD COLUMN IF NOT EXISTS reviewed_at          TIMESTAMP(6);
ALTER TABLE join_request_log_table ADD COLUMN IF NOT EXISTS reviewed_by_user_id  INTEGER;

-- persona_log_table
-- Entity: name, description, category
-- Missing: category
ALTER TABLE persona_log_table ADD COLUMN IF NOT EXISTS category VARCHAR(255);

-- player_log_table
-- Entity: name, surname, foot, photo, rating (Double), position, squad_id
-- Missing: surname, foot, photo, rating
ALTER TABLE player_log_table ADD COLUMN IF NOT EXISTS surname  VARCHAR(255);
ALTER TABLE player_log_table ADD COLUMN IF NOT EXISTS foot     VARCHAR(255);
ALTER TABLE player_log_table ADD COLUMN IF NOT EXISTS photo    VARCHAR(255);
ALTER TABLE player_log_table ADD COLUMN IF NOT EXISTS rating   DOUBLE PRECISION;

-- player_persona_log_table
-- Entity: player_id, persona_id, squad_id, count (Integer)
-- Missing: count
ALTER TABLE player_persona_log_table ADD COLUMN IF NOT EXISTS count INTEGER;

-- rating_log_table
-- Entity: rate (Integer), roster_id, player_id
-- Missing: rate, roster_id  (migration had old field names: rating_value, game_id, voter_id)
ALTER TABLE rating_log_table ADD COLUMN IF NOT EXISTS rate      INTEGER;
ALTER TABLE rating_log_table ADD COLUMN IF NOT EXISTS roster_id INTEGER;

-- roster_log_table
-- Entity: teamColor, rating (double), persona1, persona2, persona3, has_vote, game_id, player_id
-- Missing: team_color, rating, persona1, persona2, persona3, has_vote, player_id
ALTER TABLE roster_log_table ADD COLUMN IF NOT EXISTS team_color VARCHAR(255);
ALTER TABLE roster_log_table ADD COLUMN IF NOT EXISTS rating     DOUBLE PRECISION;
ALTER TABLE roster_log_table ADD COLUMN IF NOT EXISTS persona1   INTEGER;
ALTER TABLE roster_log_table ADD COLUMN IF NOT EXISTS persona2   INTEGER;
ALTER TABLE roster_log_table ADD COLUMN IF NOT EXISTS persona3   INTEGER;
ALTER TABLE roster_log_table ADD COLUMN IF NOT EXISTS has_vote   BOOLEAN;
ALTER TABLE roster_log_table ADD COLUMN IF NOT EXISTS player_id  INTEGER;

-- roster_persona_log_table
-- Entity: roster_id, persona_id, count (Integer)
-- Missing: count
ALTER TABLE roster_persona_log_table ADD COLUMN IF NOT EXISTS count INTEGER;

-- squad_request_log_table
-- Entity: name, requested_by_user_id, status, reviewedAt, reviewed_by_user_id,
--         playerName, playerSurname, playerPosition, playerFoot
-- Missing: name, requested_by_user_id, reviewed_at, reviewed_by_user_id,
--          player_name, player_surname, player_position, player_foot
-- (migration had 'user_id' which is now an extra harmless column)
ALTER TABLE squad_request_log_table ADD COLUMN IF NOT EXISTS name                  VARCHAR(255);
ALTER TABLE squad_request_log_table ADD COLUMN IF NOT EXISTS requested_by_user_id  INTEGER;
ALTER TABLE squad_request_log_table ADD COLUMN IF NOT EXISTS reviewed_at           TIMESTAMP(6);
ALTER TABLE squad_request_log_table ADD COLUMN IF NOT EXISTS reviewed_by_user_id   INTEGER;
ALTER TABLE squad_request_log_table ADD COLUMN IF NOT EXISTS player_name           VARCHAR(255);
ALTER TABLE squad_request_log_table ADD COLUMN IF NOT EXISTS player_surname        VARCHAR(255);
ALTER TABLE squad_request_log_table ADD COLUMN IF NOT EXISTS player_position       VARCHAR(255);
ALTER TABLE squad_request_log_table ADD COLUMN IF NOT EXISTS player_foot           VARCHAR(255);
