-- Düzeltme: ilk migration (16_00_00) henüz oynanmamış maçları da "tamamlandı" (99) saymıştı; onların takvimi baştan işlesin.
UPDATE game_notification_state SET last_step = -1
WHERE game_id IN (SELECT id FROM game WHERE date_time > now() AND is_voted = false);
