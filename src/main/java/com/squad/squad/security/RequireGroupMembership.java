package com.squad.squad.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to require group membership for accessing methods.
 * 
 * This annotation can be used on methods that should only be accessible
 * to users who have an active membership in a group (not in pending state).
 * 
 * Usage:
 * @RequireGroupMembership
 * public ResponseEntity<?> someMethod() { ... }
 * 
 * @RequireGroupMembership(allowPending = true)
 * public ResponseEntity<?> someOtherMethod() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireGroupMembership {
    
    /**
     * Whether to allow users in pending state (Group 0) to access the method.
     * Default is false, meaning only users with approved group membership can access.
     */
    boolean allowPending() default false;
    
    /**
     * Custom error message to show when access is denied.
     */
    String message() default "Group membership required";
}