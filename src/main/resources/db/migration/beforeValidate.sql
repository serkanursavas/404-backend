-- ============================================================
-- Flyway beforeValidate Callback
-- Her startup'ta validate'den ÖNCE çalışır.
-- Kullanım: Migration rename veya checksum düzeltmeleri için.
-- Düzeltme tamamlandıktan sonra satırı SILME — idempotent kalır.
-- ============================================================

-- [2026-03-12] V3 → timestamp naming convention rename
UPDATE flyway_schema_history
SET    script = 'V20260312_01_33_00__envers-audit-tables-001.sql'
WHERE  version = '3'
  AND  script  = 'V3__add_envers_audit_tables.sql';
