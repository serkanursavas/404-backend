package com.squad.squad.exception;

/**
 * Exception thrown when a user tries to access data from a group they don't belong to.
 * This typically occurs when tenant context isolation is violated.
 */
public class GroupAccessException extends RuntimeException {

    private final Integer requestedGroupId;
    private final Integer userGroupId;

    public GroupAccessException(String message) {
        super(message);
        this.requestedGroupId = null;
        this.userGroupId = null;
    }

    public GroupAccessException(String message, Integer requestedGroupId, Integer userGroupId) {
        super(message);
        this.requestedGroupId = requestedGroupId;
        this.userGroupId = userGroupId;
    }

    public GroupAccessException(String message, Throwable cause) {
        super(message, cause);
        this.requestedGroupId = null;
        this.userGroupId = null;
    }

    public Integer getRequestedGroupId() {
        return requestedGroupId;
    }

    public Integer getUserGroupId() {
        return userGroupId;
    }

    @Override
    public String toString() {
        if (requestedGroupId != null && userGroupId != null) {
            return super.toString() + 
                   String.format(" [User Group: %d, Requested Group: %d]", userGroupId, requestedGroupId);
        }
        return super.toString();
    }
}