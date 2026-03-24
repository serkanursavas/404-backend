-- Aynı anda yalnızca bir superadmin olabilir.
-- Partial unique index: is_super_admin = true olan sadece bir satıra izin verir.
CREATE UNIQUE INDEX IF NOT EXISTS uq_single_superadmin
    ON "user" (is_super_admin)
    WHERE is_super_admin = true;
