-- ============================================================
-- Flyway beforeValidate Callback
-- Her startup'ta validate'den ÖNCE çalışır.
--
-- Hatalı migration düzeltme akışı (DELETE yaklaşımı):
--   1. Aşağıya ekle: DELETE FROM flyway_schema_history WHERE version = '<hatalı_version>';
--   2. Migration dosyasını düzelt
--   3. Dosyayı yeni timestamp ile rename et
--   4. BE restart → Flyway fresh migration olarak çalıştırır
--
-- NOT: Migration SQL'i idempotent olmalı (IF NOT EXISTS).
-- NOT: Eklenen satırları SILME — idempotent, no-op olarak kalır.
-- ============================================================

-- Fresh DB'de flyway_schema_history henüz yoktur; tüm işlemleri
-- tablonun varlığını kontrol eden bir DO bloğuna alıyoruz (idempotent).
DO $$
BEGIN
  IF EXISTS (
    SELECT FROM information_schema.tables
    WHERE  table_schema = 'public'
    AND    table_name   = 'flyway_schema_history'
  ) THEN
    -- [2026-03-12] V3 → timestamp naming convention rename
    UPDATE flyway_schema_history
    SET    version = '20260312013300',
           script  = 'V20260312_01_33_00__envers-audit-tables-001.sql'
    WHERE  version = '3'
      AND  script  = 'V3__add_envers_audit_tables.sql';

    -- [2026-03-13] V1 + V2 → V20260101 init-schema ile konsolide edildi
    DELETE FROM flyway_schema_history WHERE version IN ('1', '2');

    -- [2026-03-22] device-tokens migration FK hatası düzeltildi → yeni timestamp ile tekrar çalışacak
    DELETE FROM flyway_schema_history WHERE version = '20260322120000';

    -- [2026-03-24] device-tokens migration staging DB'de mevcut ama JAR'da yok (uncommitted) → ignore
    DELETE FROM flyway_schema_history WHERE version = '20260322.12.30.00';
  END IF;
END $$;
