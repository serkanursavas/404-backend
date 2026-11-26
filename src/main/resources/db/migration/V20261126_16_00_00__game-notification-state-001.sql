-- Maç sonrası bildirim takvimi (VoteReminderScheduler): maç başına gönderilen son adım.
-- Mevcut maçlar "tamamlandı" (99) sayılır: yayına çıkınca geçmiş maçlar için bildirim gitmez.
CREATE TABLE IF NOT EXISTS game_notification_state (
    game_id   integer PRIMARY KEY REFERENCES game (id) ON DELETE CASCADE,
    last_step integer NOT NULL DEFAULT -1
);
INSERT INTO game_notification_state (game_id, last_step)
SELECT id, 99 FROM game
ON CONFLICT (game_id) DO NOTHING;
