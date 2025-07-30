package com.squad.squad.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to require group admin role for accessing methods.
 * 
 * This annotation can be used on methods that should only be accessible
 * to users who have GROUP_ADMIN role in at least one group, or super admins.
 * 
 * Usage:
 * @RequireGroupAdmin
 * public ResponseEntity<?> someAdminMethod() { ... }
 * 
 * @RequireGroupAdmin(requireSpecificGroup = true)
 * public ResponseEntity<?> groupSpecificMethod(@PathVariable Integer groupId) { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireGroupAdmin {
    
    /**
     * Whether to require admin role for a specific group.
     * When true, the method should have a groupId parameter that will be checked.
     * Default is false, meaning any group admin role is sufficient.
     */
    boolean requireSpecificGroup() default false;
    
    /**
     * Parameter name that contains the group ID when requireSpecificGroup is true.
     * Default is "groupId".
     */
    String groupIdParam() default "groupId";
    
    /**
     * Custom error message to show when access is denied.
     */
    String message() default "Group admin role required";
}