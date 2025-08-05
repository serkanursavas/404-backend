-- =====================================================
-- GROUP ID ENTEGRASYONU - KRONOLOJİK SQL KOMUTLARI
-- =====================================================
-- Tarih: 2025-08-04
-- Açıklama: Mevcut tablolara group_id kolonu ekleme ve gerekli güncellemeler

-- =====================================================
-- 1. ADIM: MEVCUT TABLOLARA GROUP_ID KOLONU EKLEME
-- =====================================================

-- Game tablosuna group_id ekleme
ALTER TABLE public.game ADD COLUMN group_id INTEGER NOT NULL DEFAULT 1;

-- Player tablosuna group_id ekleme
ALTER TABLE public.player ADD COLUMN group_id INTEGER NOT NULL DEFAULT 1;

-- User tablosuna group_id ekleme
ALTER TABLE public.user ADD COLUMN group_id INTEGER NOT NULL DEFAULT 1;

-- Goal tablosuna group_id ekleme
ALTER TABLE public.goal ADD COLUMN group_id INTEGER NOT NULL DEFAULT 1;

-- Roster tablosuna group_id ekleme
ALTER TABLE public.roster ADD COLUMN group_id INTEGER NOT NULL DEFAULT 1;

-- Rating tablosuna group_id ekleme
ALTER TABLE public.rating ADD COLUMN group_id INTEGER NOT NULL DEFAULT 1;

-- Roster_persona tablosuna group_id ekleme
ALTER TABLE public.roster_persona ADD COLUMN group_id INTEGER NOT NULL DEFAULT 1;

-- Player_persona tablosuna group_id ekleme
ALTER TABLE public.player_persona ADD COLUMN group_id INTEGER NOT NULL DEFAULT 1;

-- Persona tablosuna group_id ekleme
ALTER TABLE public.persona ADD COLUMN group_id INTEGER NOT NULL DEFAULT 1;

-- Game_location tablosuna group_id ekleme
ALTER TABLE public.game_location ADD COLUMN group_id INTEGER NOT NULL DEFAULT 1;

-- =====================================================
-- 2. ADIM: GROUP_ID İÇİN INDEX'LER OLUŞTURMA
-- =====================================================

-- Game tablosu için group_id index'i
CREATE INDEX idx_game_group_id ON public.game(group_id);

-- Player tablosu için group_id index'i
CREATE INDEX idx_player_group_id ON public.player(group_id);

-- User tablosu için group_id index'i
CREATE INDEX idx_user_group_id ON public.user(group_id);

-- Goal tablosu için group_id index'i
CREATE INDEX idx_goal_group_id ON public.goal(group_id);

-- Roster tablosu için group_id index'i
CREATE INDEX idx_roster_group_id ON public.roster(group_id);

-- Rating tablosu için group_id index'i
CREATE INDEX idx_rating_group_id ON public.rating(group_id);

-- Roster_persona tablosu için group_id index'i
CREATE INDEX idx_roster_persona_group_id ON public.roster_persona(group_id);

-- Player_persona tablosu için group_id index'i
CREATE INDEX idx_player_persona_group_id ON public.player_persona(group_id);

-- Persona tablosu için group_id index'i
CREATE INDEX idx_persona_group_id ON public.persona(group_id);

-- Game_location tablosu için group_id index'i
CREATE INDEX idx_game_location_group_id ON public.game_location(group_id);

-- =====================================================
-- 3. ADIM: MEVCUT VERİLERİ GROUP_ID=1 İLE GÜNCELLEME
-- =====================================================

-- Tüm mevcut kayıtları group_id=1 ile güncelle
UPDATE public.game SET group_id = 1 WHERE group_id IS NULL;
UPDATE public.player SET group_id = 1 WHERE group_id IS NULL;
UPDATE public.user SET group_id = 1 WHERE group_id IS NULL;
UPDATE public.goal SET group_id = 1 WHERE group_id IS NULL;
UPDATE public.roster SET group_id = 1 WHERE group_id IS NULL;
UPDATE public.rating SET group_id = 1 WHERE group_id IS NULL;
UPDATE public.roster_persona SET group_id = 1 WHERE group_id IS NULL;
UPDATE public.player_persona SET group_id = 1 WHERE group_id IS NULL;
UPDATE public.persona SET group_id = 1 WHERE group_id IS NULL;
UPDATE public.game_location SET group_id = 1 WHERE group_id IS NULL;

-- =====================================================
-- 4. ADIM: GROUP_ID KOLONLARINI NOT NULL YAPMA
-- =====================================================

-- Game tablosu
ALTER TABLE public.game ALTER COLUMN group_id SET NOT NULL;

-- Player tablosu
ALTER TABLE public.player ALTER COLUMN group_id SET NOT NULL;

-- User tablosu
ALTER TABLE public.user ALTER COLUMN group_id SET NOT NULL;

-- Goal tablosu
ALTER TABLE public.goal ALTER COLUMN group_id SET NOT NULL;

-- Roster tablosu
ALTER TABLE public.roster ALTER COLUMN group_id SET NOT NULL;

-- Rating tablosu
ALTER TABLE public.rating ALTER COLUMN group_id SET NOT NULL;

-- Roster_persona tablosu
ALTER TABLE public.roster_persona ALTER COLUMN group_id SET NOT NULL;

-- Player_persona tablosu
ALTER TABLE public.player_persona ALTER COLUMN group_id SET NOT NULL;

-- Persona tablosu
ALTER TABLE public.persona ALTER COLUMN group_id SET NOT NULL;

-- Game_location tablosu
ALTER TABLE public.game_location ALTER COLUMN group_id SET NOT NULL;

-- =====================================================
-- 5. ADIM: GÜVENLİK İÇİN ROW LEVEL SECURITY (RLS) EKLEME
-- =====================================================

-- Game tablosu için RLS
ALTER TABLE public.game ENABLE ROW LEVEL SECURITY;

-- Player tablosu için RLS
ALTER TABLE public.player ENABLE ROW LEVEL SECURITY;

-- User tablosu için RLS
ALTER TABLE public.user ENABLE ROW LEVEL SECURITY;

-- Goal tablosu için RLS
ALTER TABLE public.goal ENABLE ROW LEVEL SECURITY;

-- Roster tablosu için RLS
ALTER TABLE public.roster ENABLE ROW LEVEL SECURITY;

-- Rating tablosu için RLS
ALTER TABLE public.rating ENABLE ROW LEVEL SECURITY;

-- Roster_persona tablosu için RLS
ALTER TABLE public.roster_persona ENABLE ROW LEVEL SECURITY;

-- Player_persona tablosu için RLS
ALTER TABLE public.player_persona ENABLE ROW LEVEL SECURITY;

-- Persona tablosu için RLS
ALTER TABLE public.persona ENABLE ROW LEVEL SECURITY;

-- Game_location tablosu için RLS
ALTER TABLE public.game_location ENABLE ROW LEVEL SECURITY;

-- =====================================================
-- 6. ADIM: RLS POLİTİKALARI OLUŞTURMA
-- =====================================================

-- Game tablosu için RLS politikası
CREATE POLICY game_group_policy ON public.game
    FOR ALL USING (group_id = current_setting('app.current_group_id', true)::integer);

-- Player tablosu için RLS politikası
CREATE POLICY player_group_policy ON public.player
    FOR ALL USING (group_id = current_setting('app.current_group_id', true)::integer);

-- User tablosu için RLS politikası
CREATE POLICY user_group_policy ON public.user
    FOR ALL USING (group_id = current_setting('app.current_group_id', true)::integer);

-- Goal tablosu için RLS politikası
CREATE POLICY goal_group_policy ON public.goal
    FOR ALL USING (group_id = current_setting('app.current_group_id', true)::integer);

-- Roster tablosu için RLS politikası
CREATE POLICY roster_group_policy ON public.roster
    FOR ALL USING (group_id = current_setting('app.current_group_id', true)::integer);

-- Rating tablosu için RLS politikası
CREATE POLICY rating_group_policy ON public.rating
    FOR ALL USING (group_id = current_setting('app.current_group_id', true)::integer);

-- Roster_persona tablosu için RLS politikası
CREATE POLICY roster_persona_group_policy ON public.roster_persona
    FOR ALL USING (group_id = current_setting('app.current_group_id', true)::integer);

-- Player_persona tablosu için RLS politikası
CREATE POLICY player_persona_group_policy ON public.player_persona
    FOR ALL USING (group_id = current_setting('app.current_group_id', true)::integer);

-- Persona tablosu için RLS politikası
CREATE POLICY persona_group_policy ON public.persona
    FOR ALL USING (group_id = current_setting('app.current_group_id', true)::integer);

-- Game_location tablosu için RLS politikası
CREATE POLICY game_location_group_policy ON public.game_location
    FOR ALL USING (group_id = current_setting('app.current_group_id', true)::integer);

-- =====================================================
-- 7. ADIM: APP_USER'A GEREKLİ İZİNLERİ VERME
-- =====================================================

-- app_user'a tüm tablolarda okuma/yazma izni verme
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- Gelecekte oluşturulacak tablolar için varsayılan izinler
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO app_user;

-- =====================================================
-- 8. ADIM: DOĞRULAMA SORGULARI
-- =====================================================

-- Group ID'lerin doğru eklendiğini kontrol etme
SELECT 'game' as table_name, COUNT(*) as total_records, COUNT(group_id) as records_with_group_id 
FROM public.game
UNION ALL
SELECT 'player' as table_name, COUNT(*) as total_records, COUNT(group_id) as records_with_group_id 
FROM public.player
UNION ALL
SELECT 'user' as table_name, COUNT(*) as total_records, COUNT(group_id) as records_with_group_id 
FROM public.user
UNION ALL
SELECT 'goal' as table_name, COUNT(*) as total_records, COUNT(group_id) as records_with_group_id 
FROM public.goal
UNION ALL
SELECT 'roster' as table_name, COUNT(*) as total_records, COUNT(group_id) as records_with_group_id 
FROM public.roster;

-- =====================================================
-- NOTLAR:
-- =====================================================
-- 1. Bu script'i çalıştırmadan önce database backup'ı alın
-- 2. Tüm komutlar başarıyla çalıştıktan sonra uygulamayı test edin
-- 3. RLS politikaları sadece group_id=1 olan kayıtları gösterecek
-- 4. Yeni kayıtlar için group_id değeri uygulama tarafından set edilmeli
-- ===================================================== 