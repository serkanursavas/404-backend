package com.squad.squad.service;

import com.squad.squad.context.TenantContext;
import com.squad.squad.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantContextService {

    private static final Logger logger = LoggerFactory.getLogger(TenantContextService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * PostgreSQL session'a hem group_id hem user_id set eder
     */
    @Transactional
    public void setTenantContext(Integer groupId) {
        CustomUserDetails currentUser = getCurrentUser();

        if (currentUser != null) {
            // User ID'yi her zaman set et
            jdbcTemplate.execute("SET app.current_user_id = '" + currentUser.getId() + "'");

            // Group ID set et
            if (groupId != null) {
                // Grup 0 (pending) kullanıcılar için özel handling
                if (groupId == 0) {
                    // Pending kullanıcılar için restricted context
                    jdbcTemplate.execute("SET app.current_tenant_id = '0'");
                    jdbcTemplate.execute("SET app.group_id = '0'");
                    logger.info("Pending user context set for user: {}", currentUser.getId());
                } else {
                    // Normal grup kullanıcıları
                    jdbcTemplate.execute("SET app.current_tenant_id = '" + groupId + "'");
                    jdbcTemplate.execute("SET app.group_id = '" + groupId + "'");
                }
            } else {
                // Group ID null ise temizle
                jdbcTemplate.execute("SET app.current_tenant_id = ''");
                jdbcTemplate.execute("SET app.group_id = ''");
            }

            // TenantContext'i de güncelle
            TenantContext.setTenantId(groupId);
        }
    }

    /**
     * Sadece user context'i set et (grup değiştirmeden)
     */
    @Transactional
    public void setUserContext() {
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser != null) {
            jdbcTemplate.execute("SET app.current_user_id = '" + currentUser.getId() + "'");

            // Eğer tenant context yoksa, user'ın grup ID'sini set et
            if (TenantContext.getTenantId() == null) {
                setTenantContext(currentUser.getGroupId());
            }
        }
    }

    /**
     * Tenant context'i temizler
     */
    @Transactional
    public void clearTenantContext() {
        // PostgreSQL session'dan temizle
        jdbcTemplate.execute("SET app.current_tenant_id = ''");
        jdbcTemplate.execute("SET app.group_id = ''");
        jdbcTemplate.execute("SET app.current_user_id = ''");

        // TenantContext'i de temizle
        TenantContext.clear();
    }

    /**
     * Super Admin için RLS bypass
     */
    @Transactional
    public void setSuperAdminContext() {
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser != null && "ROLE_ADMIN".equals(currentUser.getRole())) {
            // User ID'yi set et
            jdbcTemplate.execute("SET app.current_user_id = '" + currentUser.getId() + "'");

            // Super Admin için özel tenant değeri (isteğe bağlı)
            jdbcTemplate.execute("SET app.current_tenant_id = 'SUPER_ADMIN'");
            jdbcTemplate.execute("SET app.group_id = 'SUPER_ADMIN'");

            // TenantContext'i null yap (bypass)
            TenantContext.clear();
        }
    }

    /**
     * Mevcut tenant context'i kontrol et
     */
    public Integer getCurrentTenantId() {
        return TenantContext.getTenantId();
    }

    /**
     * Kullanıcının Super Admin olup olmadığını kontrol et
     */
    public boolean isSuperAdmin() {
        CustomUserDetails currentUser = getCurrentUser();
        return currentUser != null && "ROLE_ADMIN".equals(currentUser.getRole());
    }

    /**
     * Mevcut kullanıcıyı al
     */
    private CustomUserDetails getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return (CustomUserDetails) principal;
            }
        } catch (Exception e) {
            // Authentication yoksa null döndür
        }
        return null;
    }

    /**
     * Authentication context'ine göre otomatik tenant set et
     */
    @Transactional
    public void initializeContext() {
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser != null) {
            if ("ROLE_ADMIN".equals(currentUser.getRole())) {
                setSuperAdminContext();
            } else {
                setTenantContext(currentUser.getGroupId());
            }
        }
    }
}