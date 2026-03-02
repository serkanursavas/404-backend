-- ============================================================
-- Multi-Tenancy Migration Script
-- Run AFTER Hibernate creates the new tables (ddl-auto=update)
-- ============================================================

-- 1. Insert default "404 Squad" group
INSERT INTO squad (name, invite_code, created_at, created_by_user_id)
SELECT '404 Squad', 'SQUAD404', NOW(), u.id
FROM "user" u
WHERE u.username = 'serkan'
ON CONFLICT DO NOTHING;

-- 2. Assign all existing players to the default squad
UPDATE player SET squad_id = (SELECT id FROM squad WHERE name = '404 Squad')
WHERE squad_id IS NULL;

-- 3. Assign all existing games to the default squad
UPDATE game SET squad_id = (SELECT id FROM squad WHERE name = '404 Squad')
WHERE squad_id IS NULL;

-- 4. Assign all existing game_locations to the default squad
UPDATE game_location SET squad_id = (SELECT id FROM squad WHERE name = '404 Squad')
WHERE squad_id IS NULL;

-- 5. Assign all existing player_persona to the default squad
UPDATE player_persona SET squad_id = (SELECT id FROM squad WHERE name = '404 Squad')
WHERE squad_id IS NULL;

-- 6. Create group_membership records from existing user-player relationships
-- (Using the old player_id column on the user table before it's dropped)
INSERT INTO group_membership (squad_id, user_id, player_id, role, joined_at)
SELECT
    (SELECT id FROM squad WHERE name = '404 Squad'),
    u.id,
    u.player_id,
    CASE
        WHEN u.role = 'ADMIN' THEN 'ADMIN'
        ELSE 'MEMBER'
    END,
    u.created_at
FROM "user" u
WHERE u.player_id IS NOT NULL
ON CONFLICT (squad_id, user_id) DO NOTHING;

-- 7. Set super admin (serkan)
UPDATE "user" SET is_super_admin = true WHERE username = 'serkan';

-- 8. Drop the old player_id column from user table (after membership migration)
-- NOTE: Run this AFTER verifying the migration data is correct!
-- ALTER TABLE "user" DROP COLUMN IF EXISTS player_id;
