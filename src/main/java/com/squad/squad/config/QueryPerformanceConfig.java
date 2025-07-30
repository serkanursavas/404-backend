package com.squad.squad.config;

import com.squad.squad.security.CustomUserDetails;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Configuration for monitoring database query performance,
 * especially for group-based queries and tenant isolation.
 */
@Aspect
@Configuration
public class QueryPerformanceConfig {

    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");
    private static final Logger slowQueryLogger = LoggerFactory.getLogger("SLOW_QUERY");

    @Value("${app.performance.slow-query-threshold-ms:1000}")
    private long slowQueryThresholdMs;

    @Value("${app.performance.monitoring.enabled:true}")
    private boolean performanceMonitoringEnabled;

    /**
     * Monitor repository method performance
     */
    @Around("execution(* com.squad.squad.repository..*(..))")
    public Object monitorRepositoryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!performanceMonitoringEnabled) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().toShortString();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        CustomUserDetails currentUser = getCurrentUser();
        
        // Setup MDC for structured logging
        MDC.put("queryType", "REPOSITORY");
        MDC.put("className", className);
        MDC.put("methodName", methodName);
        MDC.put("userId", currentUser != null ? currentUser.getId().toString() : "system");
        MDC.put("userGroupId", currentUser != null && currentUser.getGroupId() != null ? 
                currentUser.getGroupId().toString() : "none");

        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("status", "SUCCESS");
            
            // Log performance metrics
            if (executionTime > slowQueryThresholdMs) {
                slowQueryLogger.warn("Slow query detected - {}.{} took {}ms (user: {}, group: {})", 
                        className, methodName, executionTime, 
                        currentUser != null ? currentUser.getId() : "system",
                        currentUser != null ? currentUser.getGroupId() : "none");
            } else {
                performanceLogger.debug("Query performance - {}.{} took {}ms", 
                        className, methodName, executionTime);
            }
            
            return result;
            
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("status", "ERROR");
            MDC.put("errorMessage", ex.getMessage());
            
            performanceLogger.error("Query failed - {}.{} failed after {}ms with error: {}", 
                    className, methodName, executionTime, ex.getMessage());
            
            throw ex;
        } finally {
            MDC.clear();
        }
    }

    /**
     * Monitor service method performance with group context
     */
    @Around("execution(* com.squad.squad.service..*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!performanceMonitoringEnabled) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().toShortString();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        CustomUserDetails currentUser = getCurrentUser();
        
        // Skip monitoring for certain utility services to reduce noise
        if (shouldSkipMonitoring(className, methodName)) {
            return joinPoint.proceed();
        }
        
        MDC.put("queryType", "SERVICE");
        MDC.put("className", className);
        MDC.put("methodName", methodName);
        MDC.put("userId", currentUser != null ? currentUser.getId().toString() : "system");
        MDC.put("userGroupId", currentUser != null && currentUser.getGroupId() != null ? 
                currentUser.getGroupId().toString() : "none");

        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("status", "SUCCESS");
            
            // Log performance metrics
            if (executionTime > slowQueryThresholdMs) {
                slowQueryLogger.warn("Slow service call - {}.{} took {}ms (user: {}, group: {})", 
                        className, methodName, executionTime, 
                        currentUser != null ? currentUser.getId() : "system",
                        currentUser != null ? currentUser.getGroupId() : "none");
            } else if (executionTime > 100) { // Log services taking more than 100ms
                performanceLogger.info("Service performance - {}.{} took {}ms", 
                        className, methodName, executionTime);
            }
            
            return result;
            
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("status", "ERROR");
            MDC.put("errorMessage", ex.getMessage());
            
            performanceLogger.error("Service call failed - {}.{} failed after {}ms with error: {}", 
                    className, methodName, executionTime, ex.getMessage());
            
            throw ex;
        } finally {
            MDC.clear();
        }
    }

    /**
     * Monitor tenant context operations specifically
     */
    @Around("execution(* com.squad.squad.service.TenantContextService.*(..))")
    public Object monitorTenantContextPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!performanceMonitoringEnabled) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().getName();
        CustomUserDetails currentUser = getCurrentUser();
        
        MDC.put("queryType", "TENANT_CONTEXT");
        MDC.put("methodName", methodName);
        MDC.put("userId", currentUser != null ? currentUser.getId().toString() : "system");

        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("status", "SUCCESS");
            
            // Tenant context operations should be very fast
            if (executionTime > 500) {
                slowQueryLogger.warn("Slow tenant context operation - {} took {}ms (user: {})", 
                        methodName, executionTime, 
                        currentUser != null ? currentUser.getId() : "system");
            } else {
                performanceLogger.debug("Tenant context operation - {} took {}ms", 
                        methodName, executionTime);
            }
            
            return result;
            
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("status", "ERROR");
            MDC.put("errorMessage", ex.getMessage());
            
            performanceLogger.error("Tenant context operation failed - {} failed after {}ms with error: {}", 
                    methodName, executionTime, ex.getMessage());
            
            throw ex;
        } finally {
            MDC.clear();
        }
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
     * Determine if monitoring should be skipped for certain classes/methods
     */
    private boolean shouldSkipMonitoring(String className, String methodName) {
        // Skip utility services that are called frequently
        if (className.contains("TenantContextService") && 
            (methodName.contains("getCurrentUser") || methodName.contains("getCurrentTenantId"))) {
            return true;
        }
        
        // Skip getter/setter methods
        if (methodName.contains("toString()") || methodName.contains("hashCode()") || 
            methodName.contains("equals(")) {
            return true;
        }
        
        return false;
    }
}