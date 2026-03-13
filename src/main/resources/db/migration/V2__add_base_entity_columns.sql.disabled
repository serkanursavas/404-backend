-- ============================================================
-- V2: BaseEntity kolonlarını tüm tablolara ekle
-- active, created_at, updated_at, created_by_user_id, updated_by_user_id
-- IF NOT EXISTS ile güvenli - tekrar çalıştırılabilir
-- ============================================================

-- user
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);

-- squad: owner_user_id eklenir, mevcut created_by_user_id verileri taşınır
-- created_by_user_id BaseEntity tarafından audit amaçlı kullanılacak
ALTER TABLE squad ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE squad ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE squad ADD COLUMN IF NOT EXISTS owner_user_id INT REFERENCES "user"(id);
ALTER TABLE squad ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);
UPDATE squad SET owner_user_id = created_by_user_id WHERE owner_user_id IS NULL AND created_by_user_id IS NOT NULL;

-- player (active zaten var)
ALTER TABLE player ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;
ALTER TABLE player ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE player ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE player ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);

-- game
ALTER TABLE game ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE game ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;
ALTER TABLE game ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE game ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE game ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);

-- roster
ALTER TABLE roster ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE roster ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;
ALTER TABLE roster ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE roster ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE roster ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);

-- game_location
ALTER TABLE game_location ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE game_location ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;
ALTER TABLE game_location ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE game_location ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE game_location ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);

-- goal
ALTER TABLE goal ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE goal ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;
ALTER TABLE goal ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE goal ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE goal ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);

-- rating
ALTER TABLE rating ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE rating ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;
ALTER TABLE rating ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE rating ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE rating ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);

-- persona
ALTER TABLE persona ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE persona ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;
ALTER TABLE persona ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE persona ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE persona ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);

-- player_persona
ALTER TABLE player_persona ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE player_persona ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;
ALTER TABLE player_persona ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE player_persona ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE player_persona ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);

-- roster_persona
ALTER TABLE roster_persona ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE roster_persona ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;
ALTER TABLE roster_persona ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE roster_persona ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE roster_persona ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);

-- join_request (created_at zaten var)
ALTER TABLE join_request ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE join_request ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE join_request ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE join_request ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);

-- squad_request (created_at zaten var)
ALTER TABLE squad_request ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE squad_request ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE squad_request ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE squad_request ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);

-- group_membership (joined_at korunur, created_at ayrı eklenir)
ALTER TABLE group_membership ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE group_membership ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;
ALTER TABLE group_membership ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;
ALTER TABLE group_membership ADD COLUMN IF NOT EXISTS created_by_user_id INT REFERENCES "user"(id);
ALTER TABLE group_membership ADD COLUMN IF NOT EXISTS updated_by_user_id INT REFERENCES "user"(id);
