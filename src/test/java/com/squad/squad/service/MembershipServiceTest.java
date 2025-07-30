package com.squad.squad.service;

import com.squad.squad.dto.admin.AdminDecisionDTO;
import com.squad.squad.dto.membership.MembershipRequestDTO;
import com.squad.squad.dto.membership.MembershipResponseDTO;
import com.squad.squad.entity.Group;
import com.squad.squad.entity.GroupMembership;
import com.squad.squad.entity.User;
import com.squad.squad.repository.GroupMembershipRepository;
import com.squad.squad.repository.GroupRepository;
import com.squad.squad.repository.UserRepository;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.service.impl.MembershipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private GroupMembershipRepository membershipRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantContextService tenantContextService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MembershipServiceImpl membershipService;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @BeforeEach
    void setUp() {
        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext)
                .thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    void tearDown() {
        if (securityContextHolderMock != null) {
            securityContextHolderMock.close();
        }
    }

    @Test
    void testRequestMembership_Success() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 0, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        MembershipRequestDTO request = new MembershipRequestDTO();
        request.setGroupId(5);

        Group group = createMockGroup(5, Group.GroupStatus.APPROVED);
        when(groupRepository.findById(5)).thenReturn(Optional.of(group));

        when(membershipRepository.existsByUserIdAndGroupIdAndStatus(
                1, 5, GroupMembership.MembershipStatus.PENDING)).thenReturn(false);
        when(membershipRepository.existsByUserIdAndGroupIdAndStatus(
                1, 5, GroupMembership.MembershipStatus.APPROVED)).thenReturn(false);

        when(tenantContextService.getCurrentTenantId()).thenReturn(0);
        when(membershipRepository.save(any(GroupMembership.class))).thenReturn(new GroupMembership());

        // When
        String result = membershipService.requestMembership(request);

        // Then
        assertEquals("Grup üyelik başvurunuz başarıyla gönderildi. Grup admin onayını bekliyor.", result);
        verify(tenantContextService).setTenantContext(5);
        verify(tenantContextService).setTenantContext(0); // restore original tenant
        verify(membershipRepository).save(any(GroupMembership.class));
    }

    @Test
    void testRequestMembership_GroupNotFound() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 0, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        MembershipRequestDTO request = new MembershipRequestDTO();
        request.setGroupId(999);

        when(groupRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> membershipService.requestMembership(request));
        
        assertEquals("Grup bulunamadı.", exception.getMessage());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void testRequestMembership_GroupNotApproved() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 0, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        MembershipRequestDTO request = new MembershipRequestDTO();
        request.setGroupId(5);

        Group group = createMockGroup(5, Group.GroupStatus.PENDING);
        when(groupRepository.findById(5)).thenReturn(Optional.of(group));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> membershipService.requestMembership(request));

        assertEquals("Bu grup henüz onaylanmamış.", exception.getMessage());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void testRequestMembership_PendingRequestExists() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 0, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        MembershipRequestDTO request = new MembershipRequestDTO();
        request.setGroupId(5);

        Group group = createMockGroup(5, Group.GroupStatus.APPROVED);
        when(groupRepository.findById(5)).thenReturn(Optional.of(group));

        when(membershipRepository.existsByUserIdAndGroupIdAndStatus(
                1, 5, GroupMembership.MembershipStatus.PENDING)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> membershipService.requestMembership(request));

        assertEquals("Bu gruba zaten bekleyen bir başvurunuz var.", exception.getMessage());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void testRequestMembership_ApprovedMembershipExists() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 0, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        MembershipRequestDTO request = new MembershipRequestDTO();
        request.setGroupId(5);

        Group group = createMockGroup(5, Group.GroupStatus.APPROVED);
        when(groupRepository.findById(5)).thenReturn(Optional.of(group));

        when(membershipRepository.existsByUserIdAndGroupIdAndStatus(
                1, 5, GroupMembership.MembershipStatus.PENDING)).thenReturn(false);
        when(membershipRepository.existsByUserIdAndGroupIdAndStatus(
                1, 5, GroupMembership.MembershipStatus.APPROVED)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> membershipService.requestMembership(request));

        assertEquals("Bu grubun zaten üyesisiniz.", exception.getMessage());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void testGetPendingMemberships_SuperAdmin() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 1, "ROLE_ADMIN");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        List<GroupMembership> pendingMemberships = Arrays.asList(
                createMockMembership(1, 2, 5, GroupMembership.MembershipStatus.PENDING),
                createMockMembership(2, 3, 6, GroupMembership.MembershipStatus.PENDING)
        );

        when(tenantContextService.getCurrentTenantId()).thenReturn(1);
        when(membershipRepository.findByStatusNative("PENDING")).thenReturn(pendingMemberships);

        // When
        List<MembershipResponseDTO> result = membershipService.getPendingMemberships();

        // Then
        assertEquals(2, result.size());
        verify(tenantContextService).setSuperAdminContext();
        verify(tenantContextService).setTenantContext(1); // restore original tenant
    }

    @Test
    void testGetPendingMemberships_GroupAdmin() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 5, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        List<GroupMembership> adminMemberships = Arrays.asList(
                createMockMembership(3, 1, 5, GroupMembership.MembershipStatus.APPROVED, GroupMembership.MembershipRole.GROUP_ADMIN)
        );

        List<GroupMembership> pendingMemberships = Arrays.asList(
                createMockMembership(4, 2, 5, GroupMembership.MembershipStatus.PENDING)
        );

        when(membershipRepository.findByUserIdAndStatusAndRole(
                1, GroupMembership.MembershipStatus.APPROVED, GroupMembership.MembershipRole.GROUP_ADMIN))
                .thenReturn(adminMemberships);

        when(membershipRepository.findByGroupIdAndStatus(5, GroupMembership.MembershipStatus.PENDING))
                .thenReturn(pendingMemberships);

        // When
        List<MembershipResponseDTO> result = membershipService.getPendingMemberships();

        // Then
        assertEquals(1, result.size());
        verify(membershipRepository).findByUserIdAndStatusAndRole(
                1, GroupMembership.MembershipStatus.APPROVED, GroupMembership.MembershipRole.GROUP_ADMIN);
        verify(membershipRepository).findByGroupIdAndStatus(5, GroupMembership.MembershipStatus.PENDING);
    }

    @Test
    void testGetPendingMemberships_NoAdminRole() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 5, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(membershipRepository.findByUserIdAndStatusAndRole(
                1, GroupMembership.MembershipStatus.APPROVED, GroupMembership.MembershipRole.GROUP_ADMIN))
                .thenReturn(Arrays.asList()); // Boş liste - admin değil

        // When
        List<MembershipResponseDTO> result = membershipService.getPendingMemberships();

        // Then
        assertEquals(0, result.size());
        verify(membershipRepository, never()).findByGroupIdAndStatus(anyInt(), any());
    }

    @Test
    void testProcessMembershipRequest_ApprovalSuccess() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 5, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        AdminDecisionDTO decision = new AdminDecisionDTO();
        decision.setDecision("APPROVE");

        GroupMembership membership = createMockMembership(10, 2, 5, GroupMembership.MembershipStatus.PENDING);
        when(membershipRepository.findById(10)).thenReturn(Optional.of(membership));

        User user = createMockUser(2, 0);
        when(userRepository.findById(2)).thenReturn(Optional.of(user));

        // When
        String result = membershipService.processMembershipRequest(10, decision);

        // Then
        assertEquals("Üyelik başvurusu onaylandı.", result);
        assertEquals(GroupMembership.MembershipStatus.APPROVED, membership.getStatus());
        assertEquals(5, user.getGroupId());
        assertNotNull(membership.getApprovedAt());
        assertEquals(1, membership.getApprovedBy());
        verify(membershipRepository).save(membership);
        verify(userRepository).save(user);
    }

    @Test
    void testProcessMembershipRequest_RejectionSuccess() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 5, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        AdminDecisionDTO decision = new AdminDecisionDTO();
        decision.setDecision("REJECT");

        GroupMembership membership = createMockMembership(10, 2, 5, GroupMembership.MembershipStatus.PENDING);
        when(membershipRepository.findById(10)).thenReturn(Optional.of(membership));

        // When
        String result = membershipService.processMembershipRequest(10, decision);

        // Then
        assertEquals("Üyelik başvurusu reddedildi.", result);
        assertEquals(GroupMembership.MembershipStatus.REJECTED, membership.getStatus());
        assertNotNull(membership.getApprovedAt());
        assertEquals(1, membership.getApprovedBy());
        verify(membershipRepository).save(membership);
        verify(userRepository, never()).save(any()); // User güncellenmemeli
    }

    @Test
    void testProcessMembershipRequest_MembershipNotFound() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 5, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        AdminDecisionDTO decision = new AdminDecisionDTO();
        decision.setDecision("APPROVE");

        when(membershipRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> membershipService.processMembershipRequest(999, decision));

        assertEquals("Üyelik talebi bulunamadı.", exception.getMessage());
    }

    @Test
    void testProcessMembershipRequest_InvalidDecision() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 5, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        AdminDecisionDTO decision = new AdminDecisionDTO();
        decision.setDecision("INVALID");

        GroupMembership membership = createMockMembership(10, 2, 5, GroupMembership.MembershipStatus.PENDING);
        when(membershipRepository.findById(10)).thenReturn(Optional.of(membership));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> membershipService.processMembershipRequest(10, decision));

        assertEquals("Geçersiz karar: INVALID", exception.getMessage());
    }

    // Helper methods
    private CustomUserDetails createMockUser(Integer id, Integer groupId, String role) {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(id);
        when(userDetails.getGroupId()).thenReturn(groupId);
        when(userDetails.getRole()).thenReturn(role);
        when(userDetails.getUsername()).thenReturn("testuser" + id);
        return userDetails;
    }

    private User createMockUser(Integer id, Integer groupId) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getGroupId()).thenReturn(groupId);
        return user;
    }

    private Group createMockGroup(Integer id, Group.GroupStatus status) {
        Group group = mock(Group.class);
        when(group.getId()).thenReturn(id);
        when(group.getStatus()).thenReturn(status);
        return group;
    }

    private GroupMembership createMockMembership(Integer id, Integer userId, Integer groupId, 
                                                GroupMembership.MembershipStatus status) {
        return createMockMembership(id, userId, groupId, status, GroupMembership.MembershipRole.MEMBER);
    }

    private GroupMembership createMockMembership(Integer id, Integer userId, Integer groupId, 
                                                GroupMembership.MembershipStatus status, 
                                                GroupMembership.MembershipRole role) {
        GroupMembership membership = mock(GroupMembership.class);
        when(membership.getId()).thenReturn(id);
        when(membership.getUserId()).thenReturn(userId);
        when(membership.getGroupId()).thenReturn(groupId);
        when(membership.getStatus()).thenReturn(status);
        when(membership.getRole()).thenReturn(role);
        when(membership.getRequestedAt()).thenReturn(LocalDateTime.now());
        return membership;
    }
}