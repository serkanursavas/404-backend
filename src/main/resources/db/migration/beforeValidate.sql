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
-- version ve script ikisi birden güncellenmeli; idempotent (no-op eğer kayıt yoksa)
UPDATE flyway_schema_history
SET    version = '20260312013300',
       script  = 'V20260312_01_33_00__envers-audit-tables-001.sql'
WHERE  version = '3'
  AND  script  = 'V3__add_envers_audit_tables.sql';

-- [2026-03-13] V1 + V2 → V20260101 init-schema ile konsolide edildi
-- Bu dosyalar artık classpath'te yok; validate öncesi history'den temizle
-- (idempotent: kayıt yoksa no-op)
DELETE FROM flyway_schema_history WHERE version IN ('1', '2');
