package com.squad.squad.service;

import com.squad.squad.dto.admin.AdminDecisionDTO;
import com.squad.squad.dto.membership.MembershipRequestDTO;
import com.squad.squad.dto.membership.MembershipResponseDTO;

import java.util.List;

/**
 * Service interface for managing group membership operations in a multi-tenant environment.
 * 
 * <p>This service handles all aspects of group membership lifecycle including:</p>
 * <ul>
 *   <li>Processing membership requests from users</li>
 *   <li>Managing approval/rejection workflows for group administrators</li>
 *   <li>Providing membership status and history information</li>
 *   <li>Enforcing security constraints and tenant isolation</li>
 * </ul>
 * 
 * <p>The service integrates with the multi-tenant security model to ensure that:</p>
 * <ul>
 *   <li>Users can only request membership for appropriate groups</li>
 *   <li>Group administrators can only manage their own group's memberships</li>
 *   <li>All operations respect tenant boundaries and security policies</li>
 * </ul>
 * 
 * <p>Security is enforced through custom annotations and Spring Security integration,
 * with additional validation methods for fine-grained access control.</p>
 * 
 * @author Squad Development Team
 * @since 1.0.0
 * @see MembershipRequestDTO
 * @see MembershipResponseDTO
 * @see AdminDecisionDTO
 */
public interface MembershipService {

    /**
     * Submits a new group membership request.
     * 
     * <p>Allows users to request membership in a specific group. The request will be
     * placed in PENDING status and await approval from group administrators.</p>
     * 
     * <p>Validation rules:</p>
     * <ul>
     *   <li>User cannot request membership in their current group</li>
     *   <li>User cannot have multiple pending requests for the same group</li>
     *   <li>Target group must exist and be available for membership requests</li>
     * </ul>
     * 
     * @param request the membership request details including target group and optional message
     * @return success message confirming the request submission
     * @throws IllegalArgumentException if the request is invalid
     * @throws ConflictException if a pending request already exists
     */
    String requestMembership(MembershipRequestDTO request);

    /**
     * Retrieves all pending membership requests for the current user's group.
     * 
     * <p>This method is restricted to group administrators and returns all membership
     * requests that are awaiting approval or rejection. The results are automatically
     * filtered by tenant context to ensure administrators only see requests for their group.</p>
     * 
     * @return list of pending membership requests for the current group
     * @throws AccessDeniedException if the current user is not a group administrator
     */
    List<MembershipResponseDTO> getPendingMemberships();

    /**
     * Processes a membership request by approving or rejecting it.
     * 
     * <p>Group administrators can use this method to approve or reject pending
     * membership requests. Upon approval, the user's group membership is updated
     * and they gain access to the group's resources.</p>
     * 
     * <p>Security constraints:</p>
     * <ul>
     *   <li>Only group administrators can process membership requests</li>
     *   <li>Administrators can only process requests for their own group</li>
     *   <li>Requests must be in PENDING status to be processed</li>
     * </ul>
     * 
     * @param membershipId the ID of the membership request to process
     * @param decision the administrator's decision (approve/reject) with optional response message
     * @return success message confirming the processing of the request
     * @throws AccessDeniedException if the user lacks authorization to process this request
     * @throws IllegalStateException if the request is not in a processable state
     */
    String processMembershipRequest(Integer membershipId, AdminDecisionDTO decision);

    /**
     * Retrieves the membership history for a specific user.
     * 
     * <p>Returns all membership requests and their statuses for the specified user.
     * Access is restricted by tenant context - users can only view their own membership
     * history, while group administrators can view memberships within their group.</p>
     * 
     * @param userId the ID of the user whose membership history to retrieve
     * @return list of membership records for the specified user
     * @throws AccessDeniedException if the current user lacks permission to view this data
     */
    List<MembershipResponseDTO> getUserMemberships(Integer userId);

    /**
     * Retrieves all members of a specific group.
     * 
     * <p>This method is restricted to group administrators and returns information
     * about all current and past members of the specified group. The tenant context
     * ensures that administrators can only query their own group's membership.</p>
     * 
     * @param groupId the ID of the group whose members to retrieve
     * @return list of all members in the specified group
     * @throws AccessDeniedException if the current user is not an administrator of the specified group
     */
    List<MembershipResponseDTO> getGroupMembers(Integer groupId);

    /**
     * Checks if the current user has group administrator privileges.
     * 
     * <p>This method is used by Spring Security's {@code @PreAuthorize} annotations
     * to validate access to administrative operations. It verifies that the user
     * has the GROUP_ADMIN role within their current tenant context.</p>
     * 
     * @param userPrincipal the authenticated user principal from security context
     * @return {@code true} if the user is a group administrator, {@code false} otherwise
     */
    boolean isUserGroupAdmin(Object userPrincipal);

    /**
     * Checks if the current user is an administrator of a specific group.
     * 
     * <p>This method validates that the user has administrative privileges for
     * the specified group. It's used for cross-group administrative operations
     * and ensures proper authorization for group-specific actions.</p>
     * 
     * @param userPrincipal the authenticated user principal from security context
     * @param groupId the ID of the group to check administrative access for
     * @return {@code true} if the user is an administrator of the specified group, {@code false} otherwise
     */
    boolean isUserAdminOfGroup(Object userPrincipal, Integer groupId);

    /**
     * Checks if the current user can process a specific membership request.
     * 
     * <p>This method validates that the user has the necessary permissions to
     * approve or reject the specified membership request. It ensures that only
     * administrators of the target group can process membership requests.</p>
     * 
     * @param userPrincipal the authenticated user principal from security context
     * @param membershipId the ID of the membership request to check processing rights for
     * @return {@code true} if the user can process the membership request, {@code false} otherwise
     */
    boolean canUserProcessMembership(Object userPrincipal, Integer membershipId);
}