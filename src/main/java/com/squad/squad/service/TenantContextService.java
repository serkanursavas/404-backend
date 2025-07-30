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

/**
 * Service for managing tenant context in a multi-tenant application.
 * 
 * <p>This service handles the setting and clearing of tenant context information
 * in both application memory (TenantContext) and PostgreSQL session variables.
 * It's responsible for ensuring proper isolation between different tenants (groups)
 * by managing the database session state.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Setting tenant context for authenticated users</li>
 *   <li>Managing PostgreSQL session variables for Row Level Security (RLS)</li>
 *   <li>Handling special cases like pending users (Group 0) and super admins</li>
 *   <li>Providing context information for security and audit purposes</li>
 * </ul>
 * 
 * <p>The service integrates with Spring Security to automatically determine
 * the current user's context and applies appropriate database session settings
 * to ensure data isolation through PostgreSQL RLS policies.</p>
 * 
 * @author Squad Development Team
 * @since 1.0.0
 * @see TenantContext
 * @see CustomUserDetails
 */
@Service
public class TenantContextService {

    private static final Logger logger = LoggerFactory.getLogger(TenantContextService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Sets the tenant context for the current user in both application memory and database session.
     * 
     * <p>This method configures PostgreSQL session variables that are used by Row Level Security (RLS)
     * policies to ensure data isolation between different tenants. It handles special cases:</p>
     * <ul>
     *   <li><strong>Group 0 (Pending Users):</strong> Users awaiting group approval get restricted access</li>
     *   <li><strong>Regular Groups:</strong> Normal tenant isolation is applied</li>
     *   <li><strong>Null Group:</strong> Context is cleared for unauthenticated scenarios</li>
     * </ul>
     * 
     * <p>The method sets the following PostgreSQL session variables:</p>
     * <ul>
     *   <li>{@code app.current_user_id} - Current user's ID for audit purposes</li>
     *   <li>{@code app.current_tenant_id} - Current tenant/group ID for RLS policies</li>
     *   <li>{@code app.group_id} - Group ID for backward compatibility</li>
     * </ul>
     * 
     * @param groupId the ID of the group/tenant to set as context. Can be null to clear context,
     *                0 for pending users, or any positive integer for regular groups
     * @throws IllegalStateException if no authenticated user is found in security context
     * @see #clearTenantContext()
     * @see #setSuperAdminContext()
     */
    @Transactional
    public void setTenantContext(Integer groupId) {
        CustomUserDetails currentUser = getCurrentUser();

        if (currentUser != null) {
            Integer previousTenantId = TenantContext.getTenantId();
            
            logger.debug("Setting tenant context for user: {} (previous: {}, new: {})", 
                    currentUser.getId(), previousTenantId, groupId);
            
            // User ID'yi her zaman set et
            jdbcTemplate.execute("SET app.current_user_id = '" + currentUser.getId() + "'");

            // Group ID set et
            if (groupId != null) {
                // Grup 0 (pending) kullanıcılar için özel handling
                if (groupId == 0) {
                    // Pending kullanıcılar için restricted context
                    jdbcTemplate.execute("SET app.current_tenant_id = '0'");
                    jdbcTemplate.execute("SET app.group_id = '0'");
                    logger.info("Pending user context set for user: {} (groupId: 0)", currentUser.getId());
                } else {
                    // Normal grup kullanıcıları
                    jdbcTemplate.execute("SET app.current_tenant_id = '" + groupId + "'");
                    jdbcTemplate.execute("SET app.group_id = '" + groupId + "'");
                    logger.info("Tenant context set for user: {} to group: {}", currentUser.getId(), groupId);
                }
            } else {
                // Group ID null ise temizle
                jdbcTemplate.execute("SET app.current_tenant_id = ''");
                jdbcTemplate.execute("SET app.group_id = ''");
                logger.info("Tenant context cleared for user: {}", currentUser.getId());
            }

            // Context değişimi logla
            if (previousTenantId != null && !previousTenantId.equals(groupId)) {
                logger.warn("Tenant context changed for user: {} from {} to {}", 
                        currentUser.getId(), previousTenantId, groupId);
            }

            // TenantContext'i de güncelle
            TenantContext.setTenantId(groupId);
            
            logger.debug("Tenant context successfully set. User: {}, Group: {}", 
                    currentUser.getId(), groupId);
        } else {
            logger.error("Cannot set tenant context: No authenticated user found");
        }
    }

    /**
     * Sets the user context in database session without changing the current tenant context.
     * 
     * <p>This method is used to establish user identity in the database session while preserving
     * any existing tenant context. If no tenant context exists, it automatically initializes
     * the tenant context using the user's current group membership.</p>
     * 
     * <p>This is particularly useful in scenarios where:</p>
     * <ul>
     *   <li>User identity needs to be established for audit purposes</li>
     *   <li>Tenant context needs to be preserved during user operations</li>
     *   <li>Automatic tenant context initialization is desired when missing</li>
     * </ul>
     * 
     * @see #setTenantContext(Integer)
     * @see #initializeContext()
     */
    @Transactional
    public void setUserContext() {
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser != null) {
            logger.debug("Setting user context for user: {}", currentUser.getId());
            
            jdbcTemplate.execute("SET app.current_user_id = '" + currentUser.getId() + "'");

            // Eğer tenant context yoksa, user'ın grup ID'sini set et
            if (TenantContext.getTenantId() == null) {
                logger.info("No tenant context found, initializing for user: {} with group: {}", 
                        currentUser.getId(), currentUser.getGroupId());
                setTenantContext(currentUser.getGroupId());
            } else {
                logger.debug("User context set successfully for user: {}", currentUser.getId());
            }
        } else {
            logger.error("Cannot set user context: No authenticated user found");
        }
    }

    /**
     * Clears all tenant context information from both application memory and database session.
     * 
     * <p>This method removes all tenant-related session variables from PostgreSQL and clears
     * the application-level tenant context. It's typically called at the end of request processing
     * to ensure clean state for subsequent requests.</p>
     * 
     * <p>The following session variables are cleared:</p>
     * <ul>
     *   <li>{@code app.current_tenant_id} - Tenant/group context</li>
     *   <li>{@code app.group_id} - Group context</li>
     *   <li>{@code app.current_user_id} - User context</li>
     * </ul>
     * 
     * <p><strong>Note:</strong> This operation should be performed with caution as it affects
     * Row Level Security behavior for subsequent database operations in the same transaction.</p>
     * 
     * @see #setTenantContext(Integer)
     * @see TenantContext#clear()
     */
    @Transactional
    public void clearTenantContext() {
        CustomUserDetails currentUser = getCurrentUser();
        Integer currentTenantId = TenantContext.getTenantId();
        
        logger.info("Clearing tenant context for user: {} (current tenant: {})", 
                currentUser != null ? currentUser.getId() : "unknown", currentTenantId);
        
        // PostgreSQL session'dan temizle
        jdbcTemplate.execute("SET app.current_tenant_id = ''");
        jdbcTemplate.execute("SET app.group_id = ''");
        jdbcTemplate.execute("SET app.current_user_id = ''");

        // TenantContext'i de temizle
        TenantContext.clear();
        
        logger.debug("Tenant context cleared successfully");
    }

    /**
     * Sets a special context for super administrators that bypasses Row Level Security (RLS).
     * 
     * <p>This method configures a special database session state that allows super administrators
     * to access data across all tenants, effectively bypassing the multi-tenant isolation.
     * It's used for administrative operations that require cross-tenant visibility.</p>
     * 
     * <p>Security considerations:</p>
     * <ul>
     *   <li>Only users with {@code ROLE_ADMIN} role can use this context</li>
     *   <li>All operations are logged for audit purposes</li>
     *   <li>RLS policies should include specific clauses to handle SUPER_ADMIN context</li>
     * </ul>
     * 
     * <p>The method sets {@code app.current_tenant_id} to 'SUPER_ADMIN' and clears the
     * application-level tenant context to signal RLS bypass mode.</p>
     * 
     * @throws SecurityException if the current user is not a super administrator
     * @see #setTenantContext(Integer)
     * @see #isSuperAdmin()
     */
    @Transactional
    public void setSuperAdminContext() {
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser != null && "ROLE_ADMIN".equals(currentUser.getRole())) {
            logger.info("Setting super admin context for user: {} (bypassing RLS)", currentUser.getId());
            
            // User ID'yi set et
            jdbcTemplate.execute("SET app.current_user_id = '" + currentUser.getId() + "'");

            // Super Admin için özel tenant değeri (isteğe bağlı)
            jdbcTemplate.execute("SET app.current_tenant_id = 'SUPER_ADMIN'");
            jdbcTemplate.execute("SET app.group_id = 'SUPER_ADMIN'");

            // TenantContext'i null yap (bypass)
            TenantContext.clear();
            
            logger.debug("Super admin context set successfully for user: {}", currentUser.getId());
        } else {
            logger.error("Cannot set super admin context: User {} is not a super admin", 
                    currentUser != null ? currentUser.getId() : "unknown");
        }
    }

    /**
     * Retrieves the current tenant ID from the application context.
     * 
     * <p>This method provides access to the currently active tenant context without
     * requiring database queries. It's useful for validation and logging purposes.</p>
     * 
     * @return the current tenant ID, or {@code null} if no tenant context is set
     * @see TenantContext#getTenantId()
     */
    public Integer getCurrentTenantId() {
        return TenantContext.getTenantId();
    }

    /**
     * Checks if the current authenticated user has super administrator privileges.
     * 
     * <p>Super administrators have special privileges including:</p>
     * <ul>
     *   <li>Bypassing Row Level Security restrictions</li>
     *   <li>Accessing data across all tenants</li>
     *   <li>Performing system-wide administrative operations</li>
     * </ul>
     * 
     * @return {@code true} if the current user is a super administrator, {@code false} otherwise
     * @see #setSuperAdminContext()
     */
    public boolean isSuperAdmin() {
        CustomUserDetails currentUser = getCurrentUser();
        return currentUser != null && "ROLE_ADMIN".equals(currentUser.getRole());
    }

    /**
     * Retrieves the currently authenticated user from Spring Security context.
     * 
     * <p>This is a utility method that safely extracts the current user information
     * from the Spring Security authentication context. It handles cases where no
     * authentication is present or the principal is not a CustomUserDetails instance.</p>
     * 
     * @return the current authenticated user, or {@code null} if no user is authenticated
     *         or authentication context is not available
     * @see CustomUserDetails
     * @see SecurityContextHolder
     */
    private CustomUserDetails getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return (CustomUserDetails) principal;
            }
        } catch (Exception e) {
            // Authentication context might not be available (e.g., during startup, async operations)
            logger.debug("Could not retrieve current user from security context: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Automatically initializes the appropriate context based on the current user's authentication.
     * 
     * <p>This method is typically called during request processing to establish the proper
     * tenant context. It determines whether to set regular tenant context or super admin
     * context based on the user's role:</p>
     * 
     * <ul>
     *   <li><strong>Super Admin (ROLE_ADMIN):</strong> Sets super admin context with RLS bypass</li>
     *   <li><strong>Regular Users:</strong> Sets tenant context based on user's group membership</li>
     * </ul>
     * 
     * <p>This method is the primary entry point for context initialization and is usually
     * called by security filters or interceptors.</p>
     * 
     * @see #setTenantContext(Integer)
     * @see #setSuperAdminContext()
     */
    @Transactional
    public void initializeContext() {
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser != null) {
            logger.info("Initializing context for user: {} (role: {}, group: {})", 
                    currentUser.getId(), currentUser.getRole(), currentUser.getGroupId());
            
            if ("ROLE_ADMIN".equals(currentUser.getRole())) {
                setSuperAdminContext();
            } else {
                setTenantContext(currentUser.getGroupId());
            }
            
            logger.debug("Context initialization completed for user: {}", currentUser.getId());
        } else {
            logger.error("Cannot initialize context: No authenticated user found");
        }
    }
}