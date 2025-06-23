// 3. YENİ - TenantContextService - PostgreSQL session management
package com.squad.squad.service;

import com.squad.squad.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantContextService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * PostgreSQL session'a group_id set eder ve TenantContext'i günceller
     */
    @Transactional
    public void setTenantContext(Integer groupId) {
        if (groupId != null) {
            // PostgreSQL session'a group_id set et
            jdbcTemplate.execute("SET app.group_id = '" + groupId + "'");

            // TenantContext'i de güncelle (mevcut yapınızla uyumlu)
            TenantContext.setTenantId(groupId);
        }
    }

    /**
     * Tenant context'i temizler
     */
    @Transactional
    public void clearTenantContext() {
        // PostgreSQL session'dan temizle
        jdbcTemplate.execute("SET app.group_id = ''");

        // TenantContext'i de temizle
        TenantContext.clear();
    }

    /**
     * Mevcut tenant context'i kontrol et
     */
    public Integer getCurrentTenantId() {
        return TenantContext.getTenantId();
    }
}