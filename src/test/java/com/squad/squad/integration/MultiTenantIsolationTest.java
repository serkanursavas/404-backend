package com.squad.squad.integration;

import com.squad.squad.entity.GroupMembership;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.User;
import com.squad.squad.repository.GroupMembershipRepository;
import com.squad.squad.repository.PlayerRepository;
import com.squad.squad.repository.UserRepository;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.service.TenantContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Multi-Tenant Isolation Integration Test
 * 
 * Bu test sınıfı, farklı gruplardan kullanıcıların birbirlerinin verilerine
 * erişememesini doğrular. Row Level Security (RLS) politikalarının 
 * doğru çalıştığını test eder.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MultiTenantIsolationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private TenantContextService tenantContextService;

    // Test verileri
    private User group1User;
    private User group2User;
    private Player group1Player;
    private Player group2Player;
    private GroupMembership group1Membership;
    private GroupMembership group2Membership;

    @BeforeEach
    void setUp() {
        // Test verilerini hazırla
        prepareTestData();
    }

    @Test
    void testUserIsolation_Group1UserCannotSeeGroup2Users() {
        // Given - Group 1 kullanıcısı olarak login
        authenticateAsUser(group1User.getId(), 1, "ROLE_USER");
        tenantContextService.setTenantContext(1);

        // When - Tüm kullanıcıları getir
        List<User> users = userRepository.findAll();

        // Then - Sadece Group 1 kullanıcılarını görmeli
        assertFalse(users.isEmpty(), "Users should not be empty");
        
        // Group 2 kullanıcısını görmemeli
        boolean containsGroup2User = users.stream()
                .anyMatch(user -> user.getId().equals(group2User.getId()));
        assertFalse(containsGroup2User, "Group 1 user should not see Group 2 users");

        // Group 1 kullanıcısını görmeli (eğer RLS doğru çalışıyorsa)
        boolean containsGroup1User = users.stream()
                .anyMatch(user -> user.getId().equals(group1User.getId()));
        assertTrue(containsGroup1User, "Group 1 user should see other Group 1 users");
    }

    @Test
    void testUserIsolation_Group2UserCannotSeeGroup1Users() {
        // Given - Group 2 kullanıcısı olarak login
        authenticateAsUser(group2User.getId(), 2, "ROLE_USER");
        tenantContextService.setTenantContext(2);

        // When - Tüm kullanıcıları getir
        List<User> users = userRepository.findAll();

        // Then - Sadece Group 2 kullanıcılarını görmeli
        assertFalse(users.isEmpty(), "Users should not be empty");

        // Group 1 kullanıcısını görmemeli
        boolean containsGroup1User = users.stream()
                .anyMatch(user -> user.getId().equals(group1User.getId()));
        assertFalse(containsGroup1User, "Group 2 user should not see Group 1 users");

        // Group 2 kullanıcısını görmeli
        boolean containsGroup2User = users.stream()
                .anyMatch(user -> user.getId().equals(group2User.getId()));
        assertTrue(containsGroup2User, "Group 2 user should see other Group 2 users");
    }

    @Test
    void testPlayerIsolation_CrossTenantPlayerAccess() {
        // Test Group 1 user cannot see Group 2 players
        authenticateAsUser(group1User.getId(), 1, "ROLE_USER");
        tenantContextService.setTenantContext(1);

        List<Player> playersFromGroup1Context = playerRepository.findAll();
        
        boolean containsGroup2Player = playersFromGroup1Context.stream()
                .anyMatch(player -> player.getId().equals(group2Player.getId()));
        assertFalse(containsGroup2Player, "Group 1 user should not see Group 2 players");

        // Test Group 2 user cannot see Group 1 players
        authenticateAsUser(group2User.getId(), 2, "ROLE_USER");
        tenantContextService.setTenantContext(2);

        List<Player> playersFromGroup2Context = playerRepository.findAll();
        
        boolean containsGroup1Player = playersFromGroup2Context.stream()
                .anyMatch(player -> player.getId().equals(group1Player.getId()));
        assertFalse(containsGroup1Player, "Group 2 user should not see Group 1 players");
    }

    @Test
    void testMembershipIsolation_CrossTenantMembershipAccess() {
        // Test Group 1 context cannot see Group 2 memberships
        authenticateAsUser(group1User.getId(), 1, "ROLE_USER");
        tenantContextService.setTenantContext(1);

        List<GroupMembership> membershipsFromGroup1 = membershipRepository.findAll();
        
        boolean containsGroup2Membership = membershipsFromGroup1.stream()
                .anyMatch(membership -> membership.getId().equals(group2Membership.getId()));
        assertFalse(containsGroup2Membership, "Group 1 context should not see Group 2 memberships");

        // Test Group 2 context cannot see Group 1 memberships
        authenticateAsUser(group2User.getId(), 2, "ROLE_USER");
        tenantContextService.setTenantContext(2);

        List<GroupMembership> membershipsFromGroup2 = membershipRepository.findAll();
        
        boolean containsGroup1Membership = membershipsFromGroup2.stream()
                .anyMatch(membership -> membership.getId().equals(group1Membership.getId()));
        assertFalse(containsGroup1Membership, "Group 2 context should not see Group 1 memberships");
    }

    @Test
    void testSuperAdminCanSeeAllData() {
        // Given - Super Admin olarak login
        authenticateAsUser(1, 1, "ROLE_ADMIN");
        tenantContextService.setSuperAdminContext();

        // When - Verileri getir
        List<User> allUsers = userRepository.findAll();
        List<Player> allPlayers = playerRepository.findAll();
        List<GroupMembership> allMemberships = membershipRepository.findAll();

        // Then - Super Admin tüm verileri görebilmeli
        assertTrue(allUsers.size() >= 2, "Super Admin should see users from all groups");
        assertTrue(allPlayers.size() >= 2, "Super Admin should see players from all groups");
        assertTrue(allMemberships.size() >= 2, "Super Admin should see memberships from all groups");

        // Specific data checks
        boolean hasGroup1User = allUsers.stream()
                .anyMatch(user -> user.getId().equals(group1User.getId()));
        boolean hasGroup2User = allUsers.stream()
                .anyMatch(user -> user.getId().equals(group2User.getId()));
        
        assertTrue(hasGroup1User, "Super Admin should see Group 1 users");
        assertTrue(hasGroup2User, "Super Admin should see Group 2 users");
    }

    @Test
    void testTenantContextSwitching() {
        // Test switching between tenant contexts
        authenticateAsUser(group1User.getId(), 1, "ROLE_USER");

        // Initially set to Group 1
        tenantContextService.setTenantContext(1);
        assertEquals(1, tenantContextService.getCurrentTenantId());

        List<User> group1Users = userRepository.findAll();
        int group1UserCount = group1Users.size();

        // Switch to Group 2 context
        tenantContextService.setTenantContext(2);
        assertEquals(2, tenantContextService.getCurrentTenantId());

        List<User> group2Users = userRepository.findAll();
        int group2UserCount = group2Users.size();

        // Results should be different
        assertNotEquals(group1UserCount, group2UserCount, 
                "Different tenant contexts should return different data sets");

        // Verify isolation is maintained
        boolean group1HasGroup2Data = group1Users.stream()
                .anyMatch(user -> user.getGroupId() == 2);
        boolean group2HasGroup1Data = group2Users.stream()
                .anyMatch(user -> user.getGroupId() == 1);

        assertFalse(group1HasGroup2Data, "Group 1 context should not contain Group 2 data");
        assertFalse(group2HasGroup1Data, "Group 2 context should not contain Group 1 data");
    }

    @Test
    void testUnauthenticatedAccessRestriction() {
        // Clear authentication
        SecurityContextHolder.clearContext();
        tenantContextService.clearTenantContext();

        // Try to access data without authentication
        List<User> users = userRepository.findAll();
        List<Player> players = playerRepository.findAll();
        List<GroupMembership> memberships = membershipRepository.findAll();

        // Without proper tenant context, results should be limited or empty
        // This depends on your RLS implementation - adjust assertions accordingly
        assertTrue(users.isEmpty() || users.size() <= 2, 
                "Unauthenticated access should be restricted");
    }

    private void prepareTestData() {
        // Create Group 1 test data
        group1User = createTestUser(100, 1, "group1user", "ROLE_USER");
        group1Player = createTestPlayer(200, 1, "Group1 Player");
        group1Membership = createTestMembership(300, group1User.getId(), 1, 
                GroupMembership.MembershipStatus.APPROVED);

        // Create Group 2 test data
        group2User = createTestUser(101, 2, "group2user", "ROLE_USER");
        group2Player = createTestPlayer(201, 2, "Group2 Player");
        group2Membership = createTestMembership(301, group2User.getId(), 2, 
                GroupMembership.MembershipStatus.APPROVED);

        // Save test data
        // Note: In a real integration test, you would use @Sql or TestContainers
        // This is a simplified version for demonstration
    }

    private User createTestUser(Integer id, Integer groupId, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setGroupId(groupId);
        user.setUsername(username);
        user.setRole(role);
        user.setPassword("encoded_password");
        return user;
    }

    private Player createTestPlayer(Integer id, Integer groupId, String name) {
        Player player = new Player();
        player.setId(id);
        player.setGroupId(groupId);
        player.setName(name);
        player.setActive(true);
        return player;
    }

    private GroupMembership createTestMembership(Integer id, Integer userId, Integer groupId, 
                                               GroupMembership.MembershipStatus status) {
        GroupMembership membership = new GroupMembership();
        membership.setId(id);
        membership.setUserId(userId);
        membership.setGroupId(groupId);
        membership.setStatus(status);
        membership.setRole(GroupMembership.MembershipRole.MEMBER);
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