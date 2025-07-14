package com.squad.squad.util;

import com.squad.squad.context.TenantContext;
import com.squad.squad.config.RLSHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RLSContextManager {

    private final RLSHelper rlsHelper;

    @Autowired
    public RLSContextManager(RLSHelper rlsHelper) {
        this.rlsHelper = rlsHelper;
    }

    /**
     * User registration için sistem context'i set eder
     */
    public void setSystemContext() {
        try {
            TenantContext.setTenantId(1); // Sistem default group
            rlsHelper.setTenantContext();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set system RLS context", e);
        }
    }

    /**
     * Manuel tenant context set eder
     */
    public void setTenantContext(Integer tenantId) {
        try {
            TenantContext.setTenantId(tenantId);
            rlsHelper.setTenantContext();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set tenant RLS context", e);
        }
    }

    /**
     * Tüm context'leri temizler
     */
    public void clearContext() {
        try {
            TenantContext.clear();
            rlsHelper.clearTenantContext();
        } catch (Exception e) {
            // Clear işlemi fail olsa da devam et
            System.err.println("Warning: Failed to clear RLS context: " + e.getMessage());
        }
    }

    /**
     * Context ile işlem çalıştır (try-with-resources pattern)
     */
    public <T> T executeWithSystemContext(ContextOperation<T> operation) {
        setSystemContext();
        try {
            return operation.execute();
        } finally {
            clearContext();
        }
    }

    /**
     * Belirli tenant context'i ile işlem çalıştır
     */
    public <T> T executeWithTenantContext(Integer tenantId, ContextOperation<T> operation) {
        setTenantContext(tenantId);
        try {
            return operation.execute();
        } finally {
            clearContext();
        }
    }

    @FunctionalInterface
    public interface ContextOperation<T> {
        T execute();
    }
}