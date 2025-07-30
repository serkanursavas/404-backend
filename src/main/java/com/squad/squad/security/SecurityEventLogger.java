package com.squad.squad.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Security event logger for tracking security-related activities.
 * Logs unauthorized access attempts, authentication failures, and other
 * security events.
 */
@Component
public class SecurityEventLogger {

    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    /**
     * Log group access denial events
     */
    public void logGroupAccessDenied(Integer requestedGroupId, Integer userGroupId, String resource) {
        CustomUserDetails currentUser = getCurrentUser();

        MDC.put("event", "GROUP_ACCESS_DENIED");
        MDC.put("userId", currentUser != null ? currentUser.getId().toString() : "unknown");
        MDC.put("userGroupId", userGroupId != null ? userGroupId.toString() : "none");
        MDC.put("requestedGroupId", requestedGroupId != null ? requestedGroupId.toString() : "none");
        MDC.put("resource", resource);

        securityLogger.warn(
                "Group access denied - User: {} from group: {} attempted to access resource: {} of group: {}",
                currentUser != null ? currentUser.getId() : "unknown",
                userGroupId,
                resource,
                requestedGroupId);

        clearMDC();
    }

    /**
     * Log unauthorized API access attempts
     */
    public void logUnauthorizedAccess(String endpoint, String method, String userAgent, String remoteAddr) {
        CustomUserDetails currentUser = getCurrentUser();

        MDC.put("event", "UNAUTHORIZED_ACCESS");
        MDC.put("userId", currentUser != null ? currentUser.getId().toString() : "anonymous");
        MDC.put("endpoint", endpoint);
        MDC.put("method", method);
        MDC.put("userAgent", userAgent);
        MDC.put("remoteAddr", remoteAddr);

        securityLogger.warn("Unauthorized access attempt - User: {} tried to access {} {} from {} ({})",
                currentUser != null ? currentUser.getId() : "anonymous",
                method,
                endpoint,
                remoteAddr,
                userAgent);

        clearMDC();
    }

    /**
     * Log admin privilege escalation attempts
     */
    public void logPrivilegeEscalationAttempt(String attemptedAction, Integer targetGroupId) {
        CustomUserDetails currentUser = getCurrentUser();

        MDC.put("event", "PRIVILEGE_ESCALATION_ATTEMPT");
        MDC.put("userId", currentUser != null ? currentUser.getId().toString() : "unknown");
        MDC.put("userRole", currentUser != null ? currentUser.getRole() : "unknown");
        MDC.put("attemptedAction", attemptedAction);
        MDC.put("targetGroupId", targetGroupId != null ? targetGroupId.toString() : "none");

        securityLogger.error("Privilege escalation attempt - User: {} (role: {}) attempted: {} on group: {}",
                currentUser != null ? currentUser.getId() : "unknown",
                currentUser != null ? currentUser.getRole() : "unknown",
                attemptedAction,
                targetGroupId);

        clearMDC();
    }

    /**
     * Log suspicious tenant context switching
     */
    public void logSuspiciousTenantSwitch(Integer fromTenantId, Integer toTenantId, String context) {
        CustomUserDetails currentUser = getCurrentUser();

        MDC.put("event", "SUSPICIOUS_TENANT_SWITCH");
        MDC.put("userId", currentUser != null ? currentUser.getId().toString() : "unknown");
        MDC.put("fromTenantId", fromTenantId != null ? fromTenantId.toString() : "none");
        MDC.put("toTenantId", toTenantId != null ? toTenantId.toString() : "none");
        MDC.put("context", context);

        securityLogger.warn(
                "Suspicious tenant context switch - User: {} switched from tenant: {} to tenant: {} (context: {})",
                currentUser != null ? currentUser.getId() : "unknown",
                fromTenantId,
                toTenantId,
                context);

        clearMDC();
    }

    /**
     * Log successful group membership changes
     */
    public void logGroupMembershipChange(Integer userId, Integer groupId, String action, String status) {
        CustomUserDetails currentUser = getCurrentUser();

        MDC.put("event", "GROUP_MEMBERSHIP_CHANGE");
        MDC.put("actorUserId", currentUser != null ? currentUser.getId().toString() : "system");
        MDC.put("targetUserId", userId != null ? userId.toString() : "unknown");
        MDC.put("groupId", groupId != null ? groupId.toString() : "none");
        MDC.put("action", action);
        MDC.put("status", status);

        auditLogger.info("Group membership change - Actor: {} performed '{}' on user: {} for group: {} (status: {})",
                currentUser != null ? currentUser.getId() : "system",
                action,
                userId,
                groupId,
                status);

        clearMDC();
    }

    /**
     * Log authentication failures with group context
     */
    public void logAuthenticationFailure(String username, String reason, String remoteAddr) {
        MDC.put("event", "AUTHENTICATION_FAILURE");
        MDC.put("username", username);
        MDC.put("reason", reason);
        MDC.put("remoteAddr", remoteAddr);

        securityLogger.warn("Authentication failure - Username: {} failed authentication from {} (reason: {})",
                username,
                remoteAddr,
                reason);

        clearMDC();
    }

    /**
     * Log successful authentication with group info
     */
    public void logSuccessfulAuthentication(Integer userId, Integer groupId, String remoteAddr) {
        MDC.put("event", "AUTHENTICATION_SUCCESS");
        MDC.put("userId", userId != null ? userId.toString() : "unknown");
        MDC.put("groupId", groupId != null ? groupId.toString() : "none");
        MDC.put("remoteAddr", remoteAddr);

        auditLogger.info("Authentication success - User: {} (group: {}) authenticated from {}",
                userId,
                groupId,
                remoteAddr);

        clearMDC();
    }

    /**
     * Log RLS bypass events (for super admin)
     */
    public void logRLSBypass(String operation, String reason) {
        CustomUserDetails currentUser = getCurrentUser();

        MDC.put("event", "RLS_BYPASS");
        MDC.put("userId", currentUser != null ? currentUser.getId().toString() : "unknown");
        MDC.put("userRole", currentUser != null ? currentUser.getRole() : "unknown");
        MDC.put("operation", operation);
        MDC.put("reason", reason);

        auditLogger.warn("RLS bypass - User: {} (role: {}) bypassed RLS for operation: {} (reason: {})",
                currentUser != null ? currentUser.getId() : "unknown",
                currentUser != null ? currentUser.getRole() : "unknown",
                operation,
                reason);

        clearMDC();
    }

    /**
     * Log database query security events
     */
    public void logSecurityQueryEvent(String queryType, Integer affectedRows, String tenantContext) {
        CustomUserDetails currentUser = getCurrentUser();

        MDC.put("event", "SECURITY_QUERY");
        MDC.put("userId", currentUser != null ? currentUser.getId().toString() : "system");
        MDC.put("queryType", queryType);
        MDC.put("affectedRows", affectedRows != null ? affectedRows.toString() : "0");
        MDC.put("tenantContext", tenantContext);

        securityLogger.info("Security query executed - User: {} executed {} query affecting {} rows (tenant: {})",
                currentUser != null ? currentUser.getId() : "system",
                queryType,
                affectedRows,
                tenantContext);

        clearMDC();
    }

    /**
     * Get current user from security context
     */
    private CustomUserDetails getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return (CustomUserDetails) principal;
            }
        } catch (Exception e) {
            // Authentication context might not be available
        }
        return null;
    }

    /**
     * Clear MDC to prevent memory leaks
     */
    private void clearMDC() {
        MDC.clear();
    }
}