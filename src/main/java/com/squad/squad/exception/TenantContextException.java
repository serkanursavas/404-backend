package com.squad.squad.exception;

/**
 * Exception thrown when there are issues with tenant context management.
 * This includes cases where tenant context is missing, invalid, or inconsistent.
 */
public class TenantContextException extends RuntimeException {

    private final Integer expectedTenantId;
    private final Integer actualTenantId;

    public TenantContextException(String message) {
        super(message);
        this.expectedTenantId = null;
        this.actualTenantId = null;
    }

    public TenantContextException(String message, Integer expectedTenantId, Integer actualTenantId) {
        super(message);
        this.expectedTenantId = expectedTenantId;
        this.actualTenantId = actualTenantId;
    }

    public TenantContextException(String message, Throwable cause) {
        super(message, cause);
        this.expectedTenantId = null;
        this.actualTenantId = null;
    }

    public Integer getExpectedTenantId() {
        return expectedTenantId;
    }

    public Integer getActualTenantId() {
        return actualTenantId;
    }

    @Override
    public String toString() {
        if (expectedTenantId != null && actualTenantId != null) {
            return super.toString() + 
                   String.format(" [Expected: %d, Actual: %d]", expectedTenantId, actualTenantId);
        }
        return super.toString();
    }
}