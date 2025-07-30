package com.squad.squad.integration;

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
import com.squad.squad.service.MembershipService;
import com.squad.squad.service.TenantContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Group Selection Flow Integration Test
 * 
 * Bu test sınıfı, grup seçimi sürecinin tamamını test eder:
 * 1. Yeni kullanıcı kaydı (Group 0'a atama)
 * 2. Grup listesi görüntüleme
 * 3. Grup üyelik başvurusu
 * 4. Admin onayı/red
 * 5. Kullanıcının grup değişimi
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GroupSelectionFlowTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private TenantContextService tenantContextService;

    // Test verileri
    private User pendingUser;
    private User groupAdmin;
    private Group targetGroup;
    private Group approvedGroup;

    @BeforeEach
    void setUp() {
        // Test verilerini hazırla
        prepareTestData();
    }

    @Test
    void testCompleteGroupSelectionFlow_SuccessPath() {
        // Step 1: Yeni kullanıcı Group 0'da (pending) olarak başlar
        authenticateAsUser(pendingUser.getId(), 0, "ROLE_USER");
        tenantContextService.setTenantContext(0);

        assertEquals(0, pendingUser.getGroupId(), "New user should start in Group 0 (pending)");

        // Step 2: Kullanıcı onaylanmış grupları görür
        tenantContextService.setSuperAdminContext(); // Grupları görmek için
        List<Group> availableGroups = groupRepository.findByStatus(Group.GroupStatus.APPROVED);
        tenantContextService.setTenantContext(0); // Context'i geri al

        assertFalse(availableGroups.isEmpty(), "There should be approved groups available");
        assertTrue(availableGroups.contains(targetGroup), "Target group should be in available groups");

        // Step 3: Kullanıcı grup üyelik başvurusu yapar
        MembershipRequestDTO membershipRequest = new MembershipRequestDTO();
        membershipRequest.setGroupId(targetGroup.getId());

        String requestResult = membershipService.requestMembership(membershipRequest);
        assertEquals("Grup üyelik başvurunuz başarıyla gönderildi. Grup admin onayını bekliyor.", 
                requestResult);

        // Step 4: Başvuru veritabanında kaydedildi mi kontrol et
        Optional<GroupMembership> createdMembership = membershipRepository
                .findByUserIdAndGroupIdAndStatus(pendingUser.getId(), targetGroup.getId(), 
                        GroupMembership.MembershipStatus.PENDING);

        assertTrue(createdMembership.isPresent(), "Membership request should be created");
        assertEquals(GroupMembership.MembershipStatus.PENDING, createdMembership.get().getStatus());
        assertEquals(GroupMembership.MembershipRole.MEMBER, createdMembership.get().getRole());

        // Step 5: Grup admin gelen başvuruları görür
        authenticateAsUser(groupAdmin.getId(), targetGroup.getId(), "ROLE_USER");
        tenantContextService.setTenantContext(targetGroup.getId());

        List<MembershipResponseDTO> pendingMemberships = membershipService.getPendingMemberships();
        
        assertFalse(pendingMemberships.isEmpty(), "Group admin should see pending memberships");
        boolean hasPendingRequest = pendingMemberships.stream()
                .anyMatch(membership -> membership.getUserId().equals(pendingUser.getId()));
        assertTrue(hasPendingRequest, "Admin should see the pending membership request");

        // Step 6: Grup admin başvuruyu onaylar
        AdminDecisionDTO approvalDecision = new AdminDecisionDTO();
        approvalDecision.setDecision("APPROVE");

        String approvalResult = membershipService.processMembershipRequest(
                createdMembership.get().getId(), approvalDecision);
        assertEquals("Üyelik başvurusu onaylandı.", approvalResult);

        // Step 7: Üyelik durumu güncellenmiş mi kontrol et
        GroupMembership approvedMembership = membershipRepository.findById(createdMembership.get().getId())
                .orElseThrow();

        assertEquals(GroupMembership.MembershipStatus.APPROVED, approvedMembership.getStatus());
        assertNotNull(approvedMembership.getApprovedAt());
        assertEquals(groupAdmin.getId(), approvedMembership.getApprovedBy());

        // Step 8: Kullanıcının grup ID'si güncellenmiş mi kontrol et
        User updatedUser = userRepository.findById(pendingUser.getId()).orElseThrow();
        assertEquals(targetGroup.getId(), updatedUser.getGroupId(), 
                "User's group ID should be updated after approval");

        // Step 9: Kullanıcı artık hedef grubun verilerine erişebilir
        authenticateAsUser(pendingUser.getId(), targetGroup.getId(), "ROLE_USER");
        tenantContextService.setTenantContext(targetGroup.getId());

        List<User> groupUsers = userRepository.findAll();
        boolean canSeeGroupUsers = groupUsers.stream()
                .anyMatch(user -> user.getGroupId().equals(targetGroup.getId()));
        assertTrue(canSeeGroupUsers, "User should now see other users in the same group");
    }

    @Test
    void testGroupSelectionFlow_RejectionPath() {
        // Step 1-4: Başvuru sürecini tamamla (önceki testteki gibi)
        authenticateAsUser(pendingUser.getId(), 0, "ROLE_USER");
        tenantContextService.setTenantContext(0);

        MembershipRequestDTO membershipRequest = new MembershipRequestDTO();
        membershipRequest.setGroupId(targetGroup.getId());
        membershipService.requestMembership(membershipRequest);

        Optional<GroupMembership> createdMembership = membershipRepository
                .findByUserIdAndGroupIdAndStatus(pendingUser.getId(), targetGroup.getId(), 
                        GroupMembership.MembershipStatus.PENDING);
        assertTrue(createdMembership.isPresent());

        // Step 5: Grup admin başvuruyu reddeder
        authenticateAsUser(groupAdmin.getId(), targetGroup.getId(), "ROLE_USER");
        tenantContextService.setTenantContext(targetGroup.getId());

        AdminDecisionDTO rejectionDecision = new AdminDecisionDTO();
        rejectionDecision.setDecision("REJECT");

        String rejectionResult = membershipService.processMembershipRequest(
                createdMembership.get().getId(), rejectionDecision);
        assertEquals("Üyelik başvurusu reddedildi.", rejectionResult);

        // Step 6: Üyelik durumu reddedildi olarak güncellenmiş mi kontrol et
        GroupMembership rejectedMembership = membershipRepository.findById(createdMembership.get().getId())
                .orElseThrow();

        assertEquals(GroupMembership.MembershipStatus.REJECTED, rejectedMembership.getStatus());
        assertNotNull(rejectedMembership.getApprovedAt());
        assertEquals(groupAdmin.getId(), rejectedMembership.getApprovedBy());

        // Step 7: Kullanıcının grup ID'si değişmemiş olmalı (hala Group 0'da)
        User unchangedUser = userRepository.findById(pendingUser.getId()).orElseThrow();
        assertEquals(0, unchangedUser.getGroupId(), 
                "User should remain in Group 0 after rejection");
    }

    @Test
    void testDuplicateMembershipRequest_ShouldFail() {
        // Step 1: İlk başvuruyu yap
        authenticateAsUser(pendingUser.getId(), 0, "ROLE_USER");
        tenantContextService.setTenantContext(0);

        MembershipRequestDTO membershipRequest = new MembershipRequestDTO();
        membershipRequest.setGroupId(targetGroup.getId());
        membershipService.requestMembership(membershipRequest);

        // Step 2: Aynı gruba tekrar başvur
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            membershipService.requestMembership(membershipRequest);
        });

        assertEquals("Bu gruba zaten bekleyen bir başvurunuz var.", exception.getMessage());
    }

    @Test
    void testMembershipRequestToUnapprovedGroup_ShouldFail() {
        // Onaylanmamış grup oluştur
        Group unapprovedGroup = createTestGroup(999, "Unapproved Group", Group.GroupStatus.PENDING);
        
        // Başvuru yap
        authenticateAsUser(pendingUser.getId(), 0, "ROLE_USER");
        tenantContextService.setTenantContext(0);

        MembershipRequestDTO membershipRequest = new MembershipRequestDTO();
        membershipRequest.setGroupId(unapprovedGroup.getId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            membershipService.requestMembership(membershipRequest);
        });

        assertEquals("Bu grup henüz onaylanmamış.", exception.getMessage());
    }

    @Test
    void testApprovedUserCannotRequestSameGroupAgain() {
        // Kullanıcıyı bir gruba onaylanmış üye yap
        GroupMembership existingMembership = new GroupMembership();
        existingMembership.setUserId(pendingUser.getId());
        existingMembership.setGroupId(targetGroup.getId());
        existingMembership.setStatus(GroupMembership.MembershipStatus.APPROVED);
        existingMembership.setRole(GroupMembership.MembershipRole.MEMBER);
        membershipRepository.save(existingMembership);

        // Aynı gruba tekrar başvur
        authenticateAsUser(pendingUser.getId(), targetGroup.getId(), "ROLE_USER");
        tenantContextService.setTenantContext(targetGroup.getId());

        MembershipRequestDTO membershipRequest = new MembershipRequestDTO();
        membershipRequest.setGroupId(targetGroup.getId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            membershipService.requestMembership(membershipRequest);
        });

        assertEquals("Bu grubun zaten üyesisiniz.", exception.getMessage());
    }

    @Test
    void testSuperAdminCanSeeAllPendingMemberships() {
        // Farklı gruplara başvurular oluştur
        createTestMembership(800, pendingUser.getId(), targetGroup.getId(), 
                GroupMembership.MembershipStatus.PENDING);
        createTestMembership(801, pendingUser.getId(), approvedGroup.getId(), 
                GroupMembership.MembershipStatus.PENDING);

        // Super Admin olarak login
        authenticateAsUser(1, 1, "ROLE_ADMIN");
        tenantContextService.setSuperAdminContext();

        List<MembershipResponseDTO> allPendingMemberships = membershipService.getPendingMemberships();

        // Super Admin tüm bekleyen başvuruları görmeli
        assertTrue(allPendingMemberships.size() >= 2, 
                "Super Admin should see pending memberships from all groups");
    }

    @Test
    void testNonAdminUserCannotSeePendingMemberships() {
        // Normal kullanıcı (admin olmayan) olarak login
        authenticateAsUser(pendingUser.getId(), targetGroup.getId(), "ROLE_USER");
        tenantContextService.setTenantContext(targetGroup.getId());

        List<MembershipResponseDTO> pendingMemberships = membershipService.getPendingMemberships();

        // Normal kullanıcı bekleyen başvuruları göremez
        assertTrue(pendingMemberships.isEmpty(), 
                "Non-admin user should not see any pending memberships");
    }

    private void prepareTestData() {
        // Pending kullanıcı oluştur (Group 0'da)
        pendingUser = createTestUser(500, 0, "pendinguser", "ROLE_USER");

        // Hedef grup oluştur
        targetGroup = createTestGroup(10, "Target Group", Group.GroupStatus.APPROVED);
        
        // Diğer onaylanmış grup
        approvedGroup = createTestGroup(11, "Another Group", Group.GroupStatus.APPROVED);

        // Grup admini oluştur
        groupAdmin = createTestUser(501, targetGroup.getId(), "groupadmin", "ROLE_USER");
        
        // Grup adminini GROUP_ADMIN rolü ile üye yap
        GroupMembership adminMembership = createTestMembership(700, groupAdmin.getId(), 
                targetGroup.getId(), GroupMembership.MembershipStatus.APPROVED);
        adminMembership.setRole(GroupMembership.MembershipRole.GROUP_ADMIN);
    }

    private User createTestUser(Integer id, Integer groupId, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setGroupId(groupId);
        user.setUsername(username);
        user.setRole(role);
        user.setPassword("encoded_password");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private Group createTestGroup(Integer id, String name, Group.GroupStatus status) {
        Group group = new Group();
        group.setId(id);
        group.setName(name);
        group.setDescription("Test group description");
        group.setStatus(status);
        group.setCreatedBy(1);
        group.setGroupAdmin(1);
        group.setCreatedAt(LocalDateTime.now());
        return group;
    }

    private GroupMembership createTestMembership(Integer id, Integer userId, Integer groupId, 
                                               GroupMembership.MembershipStatus status) {
        GroupMembership membership = new GroupMembership();
        membership.setId(id);
        membership.setUserId(userId);
        membership.setGroupId(groupId);
        membership.setStatus(status);
        membership.setRole(GroupMembership.MembershipRole.MEMBER);
        membership.setRequestedAt(LocalDateTime.now());
        return membership;
    }

    private void authenticateAsUser(Integer userId, Integer groupId, String role) {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(userId);
        when(userDetails.getGroupId()).thenReturn(groupId);
        when(userDetails.getRole()).thenReturn(role);
        when(userDetails.getUsername()).thenReturn("testuser" + userId);

        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, null);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}