ALTER TABLE player ADD COLUMN IF NOT EXISTS is_guest boolean NOT NULL DEFAULT false;
ALTER TABLE player_log_table ADD COLUMN IF NOT EXISTS is_guest boolean;
