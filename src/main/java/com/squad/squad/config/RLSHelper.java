package com.squad.squad.config;

import com.squad.squad.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class RLSHelper {

    private static final Logger logger = LoggerFactory.getLogger(RLSHelper.class);

    @Autowired
    private DataSource dataSource;

    /**
     * Mevcut tenant context'e göre PostgreSQL session'ında RLS parametresini set eder
     */
    @Transactional
    public void setTenantContext() {
        Integer tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            logger.warn("Tenant context is null, skipping RLS setup");
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            setTenantContextForConnection(connection, tenantId);
            logger.debug("RLS tenant context set to: {}", tenantId);
        } catch (SQLException e) {
            logger.error("Failed to set RLS tenant context", e);
            throw new RuntimeException("Failed to set RLS tenant context", e);
        }
    }

    /**
     * Belirli bir connection için tenant context set eder
     */
    public void setTenantContextForConnection(Connection connection, Integer tenantId) throws SQLException {
        // SET komutu için Statement kullan (prepared statement yerine)
        try (var statement = connection.createStatement()) {
            String sql = String.format("SET app.current_tenant_id = %d", tenantId);
            statement.execute(sql);
        }
    }

    /**
     * PostgreSQL session'ındaki RLS parametresini temizler
     */
    @Transactional
    public void clearTenantContext() {
        try (Connection connection = dataSource.getConnection()) {
            clearTenantContextForConnection(connection);
            logger.debug("RLS tenant context cleared");
        } catch (SQLException e) {
            logger.error("Failed to clear RLS tenant context", e);
            // Clear işlemi kritik değil, session sonunda otomatik temizlenecek
        }
    }

    /**
     * Belirli bir connection için tenant context temizler
     */
    public void clearTenantContextForConnection(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("RESET app.current_tenant_id");
        }
    }

    /**
     * Mevcut PostgreSQL session'ındaki tenant context'i kontrol eder
     */
    public Integer getCurrentTenantFromDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                var resultSet = statement.executeQuery("SELECT current_setting('app.current_tenant_id', true)");
                if (resultSet.next()) {
                    String value = resultSet.getString(1);
                    return value != null && !value.isEmpty() ? Integer.parseInt(value) : null;
                }
            }
        } catch (SQLException | NumberFormatException e) {
            logger.debug("Could not retrieve current tenant from database", e);
        }
        return null;
    }

    /**
     * Debug amaçlı: Hem Java context hem de DB context'i loglar
     */
    public void logTenantContext() {
        Integer javaTenant = TenantContext.getTenantId();
        Integer dbTenant = getCurrentTenantFromDatabase();

        logger.info("Tenant Context - Java: {}, Database: {}", javaTenant, dbTenant);

        if (javaTenant != null && dbTenant != null && !javaTenant.equals(dbTenant)) {
            logger.warn("Tenant context mismatch! Java: {}, Database: {}", javaTenant, dbTenant);
        }
    }
}