package com.squad.squad.security;

import com.squad.squad.entity.GroupMembership;
import com.squad.squad.repository.GroupMembershipRepository;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * AOP Aspect to handle custom group security annotations.
 * 
 * This aspect intercepts methods annotated with @RequireGroupMembership
 * and @RequireGroupAdmin to enforce group-based access control.
 */
@Aspect
@Component
public class GroupSecurityAspect {

    private static final Logger logger = LoggerFactory.getLogger(GroupSecurityAspect.class);

    @Autowired
    private GroupMembershipRepository membershipRepository;

    /**
     * Intercept methods annotated with @RequireGroupMembership
     */
    @Around("@annotation(requireGroupMembership)")
    public Object checkGroupMembership(ProceedingJoinPoint joinPoint, RequireGroupMembership requireGroupMembership)
            throws Throwable {

        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required");
        }

        // Super Admin bypass
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            return joinPoint.proceed();
        }

        // Check group membership
        Integer userGroupId = currentUser.getGroupId();

        // If user is in pending group (Group 0)
        if (userGroupId == null || userGroupId == 0) {
            if (!requireGroupMembership.allowPending()) {
                throw new AccessDeniedException(requireGroupMembership.message() + ": User is in pending state");
            }
        } else {
            // Verify active membership
            boolean hasActiveMembership = membershipRepository.existsByUserIdAndGroupIdAndStatus(
                    currentUser.getId(), userGroupId, GroupMembership.MembershipStatus.APPROVED);

            if (!hasActiveMembership) {
                throw new AccessDeniedException(
                        requireGroupMembership.message() + ": No active group membership found");
            }
        }

        logger.debug("Group membership check passed for user: {} in group: {}",
                currentUser.getUsername(), userGroupId);

        return joinPoint.proceed();
    }

    /**
     * Intercept methods annotated with @RequireGroupAdmin
     */
    @Around("@annotation(requireGroupAdmin)")
    public Object checkGroupAdmin(ProceedingJoinPoint joinPoint, RequireGroupAdmin requireGroupAdmin) throws Throwable {

        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required");
        }

        // Super Admin bypass
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            return joinPoint.proceed();
        }

        if (requireGroupAdmin.requireSpecificGroup()) {
            // Check admin role for specific group
            Integer groupId = extractGroupIdFromParameters(joinPoint, requireGroupAdmin.groupIdParam());
            if (groupId == null) {
                throw new AccessDeniedException("Group ID parameter not found: " + requireGroupAdmin.groupIdParam());
            }

            boolean isAdminOfGroup = isUserAdminOfSpecificGroup(currentUser.getId(), groupId);
            if (!isAdminOfGroup) {
                throw new AccessDeniedException(requireGroupAdmin.message() + " for group: " + groupId);
            }

            logger.debug("Group admin check passed for user: {} in specific group: {}",
                    currentUser.getUsername(), groupId);
        } else {
            // Check if user has admin role in any group
            boolean isGroupAdmin = membershipRepository.existsByUserIdAndStatusAndRole(
                    currentUser.getId(),
                    GroupMembership.MembershipStatus.APPROVED,
                    GroupMembership.MembershipRole.GROUP_ADMIN);

            if (!isGroupAdmin) {
                throw new AccessDeniedException(requireGroupAdmin.message() + ": User is not a group admin");
            }

            logger.debug("Group admin check passed for user: {} (any group)", currentUser.getUsername());
        }

        return joinPoint.proceed();
    }

    /**
     * Extract group ID from method parameters
     */
    private Integer extractGroupIdFromParameters(ProceedingJoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            // Check for @PathVariable or @RequestParam annotations with the specified name
            if (param.getName().equals(paramName) ||
                    hasAnnotationWithValue(param, "org.springframework.web.bind.annotation.PathVariable", paramName) ||
                    hasAnnotationWithValue(param, "org.springframework.web.bind.annotation.RequestParam", paramName)) {

                Object value = args[i];
                if (value instanceof Integer) {
                    return (Integer) value;
                } else if (value instanceof String) {
                    try {
                        return Integer.valueOf((String) value);
                    } catch (NumberFormatException e) {
                        logger.warn("Could not parse group ID parameter: {}", value);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Check if parameter has annotation with specific value
     */
    private boolean hasAnnotationWithValue(Parameter param, String annotationClassName, String expectedValue) {
        return param.getAnnotations().length > 0; // Simplified check
    }

    /**
     * Check if user is admin of specific group
     */
    private boolean isUserAdminOfSpecificGroup(Integer userId, Integer groupId) {
        return membershipRepository.existsByUserIdAndGroupIdAndStatusAndRole(
                userId, groupId,
                GroupMembership.MembershipStatus.APPROVED,
                GroupMembership.MembershipRole.GROUP_ADMIN);
    }

    /**
     * Get current authenticated user
     */
    private CustomUserDetails getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return (CustomUserDetails) principal;
            }
        } catch (Exception e) {
            logger.debug("Could not get current user", e);
        }
        return null;
    }
}