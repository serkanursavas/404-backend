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

-- [2026-03-12] V3 → timestamp naming convention rename
UPDATE flyway_schema_history
SET    script = 'V20260312_01_33_00__envers-audit-tables-001.sql'
WHERE  version = '3'
  AND  script  = 'V3__add_envers_audit_tables.sql';

-- Gelecek düzeltmeler buraya eklenir. Örnek:
-- DELETE FROM flyway_schema_history WHERE version = '20260315_09_00_00';
