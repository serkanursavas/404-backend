package com.squad.squad.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GroupMembershipTest {

    private GroupMembership groupMembership;

    @BeforeEach
    void setUp() {
        groupMembership = new GroupMembership();
    }

    @Test
    void testDefaultConstructor() {
        // When
        GroupMembership membership = new GroupMembership();

        // Then
        assertNull(membership.getId());
        assertNull(membership.getUserId());
        assertEquals(GroupMembership.MembershipStatus.PENDING, membership.getStatus());
        assertEquals(GroupMembership.MembershipRole.MEMBER, membership.getRole());
        assertNotNull(membership.getRequestedAt());
        assertNull(membership.getApprovedAt());
        assertNull(membership.getApprovedBy());
    }

    @Test
    void testParameterizedConstructor() {
        // Given
        Integer userId = 10;
        Integer groupId = 5;

        // When
        GroupMembership membership = new GroupMembership(userId, groupId);

        // Then
        assertEquals(userId, membership.getUserId());
        assertEquals(groupId, membership.getGroupId());
        assertEquals(GroupMembership.MembershipStatus.PENDING, membership.getStatus());
        assertEquals(GroupMembership.MembershipRole.MEMBER, membership.getRole());
        assertNotNull(membership.getRequestedAt());
    }

    @Test
    void testIdGetterSetter() {
        // Given
        Integer expectedId = 100;

        // When
        groupMembership.setId(expectedId);

        // Then
        assertEquals(expectedId, groupMembership.getId());
    }

    @Test
    void testUserIdGetterSetter() {
        // Given
        Integer expectedUserId = 25;

        // When
        groupMembership.setUserId(expectedUserId);

        // Then
        assertEquals(expectedUserId, groupMembership.getUserId());
    }

    @Test
    void testStatusGetterSetter() {
        // Given
        GroupMembership.MembershipStatus expectedStatus = GroupMembership.MembershipStatus.APPROVED;

        // When
        groupMembership.setStatus(expectedStatus);

        // Then
        assertEquals(expectedStatus, groupMembership.getStatus());
    }

    @Test
    void testRoleGetterSetter() {
        // Given
        GroupMembership.MembershipRole expectedRole = GroupMembership.MembershipRole.GROUP_ADMIN;

        // When
        groupMembership.setRole(expectedRole);

        // Then
        assertEquals(expectedRole, groupMembership.getRole());
    }

    @Test
    void testRequestedAtGetterSetter() {
        // Given
        LocalDateTime expectedRequestedAt = LocalDateTime.of(2024, 1, 15, 10, 30);

        // When
        groupMembership.setRequestedAt(expectedRequestedAt);

        // Then
        assertEquals(expectedRequestedAt, groupMembership.getRequestedAt());
    }

    @Test
    void testApprovedAtGetterSetter() {
        // Given
        LocalDateTime expectedApprovedAt = LocalDateTime.of(2024, 1, 16, 14, 45);

        // When
        groupMembership.setApprovedAt(expectedApprovedAt);

        // Then
        assertEquals(expectedApprovedAt, groupMembership.getApprovedAt());
    }

    @Test
    void testApprovedByGetterSetter() {
        // Given
        Integer expectedApprovedBy = 99;

        // When
        groupMembership.setApprovedBy(expectedApprovedBy);

        // Then
        assertEquals(expectedApprovedBy, groupMembership.getApprovedBy());
    }

    @Test
    void testMembershipStatusEnum() {
        // Test all enum values exist
        GroupMembership.MembershipStatus[] statuses = GroupMembership.MembershipStatus.values();
        
        assertEquals(3, statuses.length);
        assertTrue(containsStatus(statuses, GroupMembership.MembershipStatus.PENDING));
        assertTrue(containsStatus(statuses, GroupMembership.MembershipStatus.APPROVED));
        assertTrue(containsStatus(statuses, GroupMembership.MembershipStatus.REJECTED));
    }

    @Test
    void testMembershipRoleEnum() {
        // Test all enum values exist
        GroupMembership.MembershipRole[] roles = GroupMembership.MembershipRole.values();
        
        assertEquals(2, roles.length);
        assertTrue(containsRole(roles, GroupMembership.MembershipRole.MEMBER));
        assertTrue(containsRole(roles, GroupMembership.MembershipRole.GROUP_ADMIN));
    }

    @Test
    void testDefaultStatusIsPending() {
        // When
        GroupMembership membership = new GroupMembership();

        // Then
        assertEquals(GroupMembership.MembershipStatus.PENDING, membership.getStatus());
    }

    @Test
    void testDefaultRoleIsMember() {
        // When
        GroupMembership membership = new GroupMembership();

        // Then
        assertEquals(GroupMembership.MembershipRole.MEMBER, membership.getRole());
    }

    @Test
    void testRequestedAtIsSetByDefault() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        
        // When
        GroupMembership membership = new GroupMembership();
        
        // Then
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertNotNull(membership.getRequestedAt());
        assertTrue(membership.getRequestedAt().isAfter(before));
        assertTrue(membership.getRequestedAt().isBefore(after));
    }

    @Test
    void testInheritanceFromBaseEntity() {
        // Given
        GroupMembership membership = new GroupMembership();
        Integer expectedGroupId = 15;

        // When
        membership.setGroupId(expectedGroupId);

        // Then
        assertEquals(expectedGroupId, membership.getGroupId());
        assertInstanceOf(BaseEntity.class, membership);
    }

    @Test
    void testFullMembershipWorkflow() {
        // Given - Initial state
        GroupMembership membership = new GroupMembership(100, 5);
        
        // Then - Verify initial state
        assertEquals(100, membership.getUserId());
        assertEquals(5, membership.getGroupId());
        assertEquals(GroupMembership.MembershipStatus.PENDING, membership.getStatus());
        assertEquals(GroupMembership.MembershipRole.MEMBER, membership.getRole());
        assertNotNull(membership.getRequestedAt());
        assertNull(membership.getApprovedAt());
        assertNull(membership.getApprovedBy());

        // When - Approve membership
        LocalDateTime approvalTime = LocalDateTime.now();
        Integer adminId = 1;
        membership.setStatus(GroupMembership.MembershipStatus.APPROVED);
        membership.setApprovedAt(approvalTime);
        membership.setApprovedBy(adminId);
        membership.setRole(GroupMembership.MembershipRole.GROUP_ADMIN);

        // Then - Verify approved state
        assertEquals(GroupMembership.MembershipStatus.APPROVED, membership.getStatus());
        assertEquals(approvalTime, membership.getApprovedAt());
        assertEquals(adminId, membership.getApprovedBy());
        assertEquals(GroupMembership.MembershipRole.GROUP_ADMIN, membership.getRole());
    }

    @Test
    void testRejectionWorkflow() {
        // Given
        GroupMembership membership = new GroupMembership(200, 10);
        LocalDateTime rejectionTime = LocalDateTime.now();
        Integer adminId = 2;

        // When - Reject membership
        membership.setStatus(GroupMembership.MembershipStatus.REJECTED);
        membership.setApprovedAt(rejectionTime); // approval time is also used for rejection time
        membership.setApprovedBy(adminId);

        // Then
        assertEquals(GroupMembership.MembershipStatus.REJECTED, membership.getStatus());
        assertEquals(rejectionTime, membership.getApprovedAt());
        assertEquals(adminId, membership.getApprovedBy());
        assertEquals(GroupMembership.MembershipRole.MEMBER, membership.getRole()); // Role remains default
    }

    // Helper methods
    private boolean containsStatus(GroupMembership.MembershipStatus[] statuses, GroupMembership.MembershipStatus status) {
        for (GroupMembership.MembershipStatus s : statuses) {
            if (s == status) {
                return true;
            }
        }
        return false;
    }

    private boolean containsRole(GroupMembership.MembershipRole[] roles, GroupMembership.MembershipRole role) {
        for (GroupMembership.MembershipRole r : roles) {
            if (r == role) {
                return true;
            }
        }
        return false;
    }
}