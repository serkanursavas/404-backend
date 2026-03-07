package com.squad.squad.service;

import com.squad.squad.context.GroupContext;
import com.squad.squad.entity.*;
import com.squad.squad.enums.GroupRole;
import com.squad.squad.enums.RequestStatus;
import com.squad.squad.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SquadService {

    private final SquadRepository squadRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final SquadRequestRepository squadRequestRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final GroupAuthorizationService authService;

    public SquadService(SquadRepository squadRepository, GroupMembershipRepository groupMembershipRepository,
                        SquadRequestRepository squadRequestRepository, JoinRequestRepository joinRequestRepository,
                        UserRepository userRepository, PlayerRepository playerRepository,
                        GroupAuthorizationService authService) {
        this.squadRepository = squadRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.squadRequestRepository = squadRequestRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.authService = authService;
    }

    // ==================== General (auth required, no group context) ====================

    @Transactional
    public SquadRequest requestCreateSquad(String squadName, String playerName, String playerSurname,
                                           String playerPosition, String playerFoot) {
        Integer userId = authService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Check for existing pending request
        if (squadRequestRepository.existsByRequestedByUserIdAndStatus(userId, RequestStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending squad creation request");
        }

        SquadRequest request = new SquadRequest();
        request.setName(squadName);
        request.setRequestedByUser(user);
        request.setPlayerName(playerName);
        request.setPlayerSurname(playerSurname);
        request.setPlayerPosition(playerPosition);
        request.setPlayerFoot(playerFoot);

        return squadRequestRepository.save(request);
    }

    @Transactional
    public JoinRequest requestJoinSquad(String inviteCode, String playerName, String playerSurname,
                                        String playerPosition, String playerFoot) {
        Integer userId = authService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Squad squad = squadRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        // Check if already a member
        if (groupMembershipRepository.existsBySquadIdAndUserId(squad.getId(), userId)) {
            throw new IllegalStateException("You are already a member of this squad");
        }

        // Check for existing pending request for this squad
        if (joinRequestRepository.existsByUserIdAndSquadIdAndStatus(userId, squad.getId(), RequestStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending join request for this squad");
        }

        long rejectedCount = joinRequestRepository.countByUserIdAndSquadIdAndStatus(
                userId, squad.getId(), RequestStatus.REJECTED);
        if (rejectedCount >= 3) {
            throw new IllegalStateException("You have been rejected too many times from this squad");
        }

        JoinRequest request = new JoinRequest();
        request.setSquad(squad);
        request.setUser(user);
        request.setPlayerName(playerName);
        request.setPlayerSurname(playerSurname);
        request.setPlayerPosition(playerPosition);
        request.setPlayerFoot(playerFoot);

        return joinRequestRepository.save(request);
    }

    public List<GroupMembership> getMySquads() {
        Integer userId = authService.getCurrentUserId();
        return groupMembershipRepository.findByUserId(userId);
    }

    public List<Object[]> getMyRequests() {
        Integer userId = authService.getCurrentUserId();
        List<SquadRequest> squadRequests = squadRequestRepository.findByRequestedByUserId(userId);
        List<JoinRequest> joinRequests = joinRequestRepository.findByUserId(userId);

        // Return both types as Object arrays for simplicity
        return List.of(
                new Object[]{"squadRequests", squadRequests},
                new Object[]{"joinRequests", joinRequests}
        );
    }

    // ==================== Group Admin (group context required) ====================

    public Squad getCurrentSquad() {
        Integer groupId = GroupContext.getCurrentGroupId();
        return squadRepository.findById(groupId)
                .orElseThrow(() -> new IllegalStateException("Squad not found"));
    }

    public List<GroupMembership> getMembers() {
        authService.requireAdmin();
        Integer groupId = GroupContext.getCurrentGroupId();
        return groupMembershipRepository.findBySquadId(groupId);
    }

    public List<JoinRequest> getPendingJoinRequests() {
        authService.requireAdmin();
        Integer groupId = GroupContext.getCurrentGroupId();
        return joinRequestRepository.findBySquadIdAndStatus(groupId, RequestStatus.PENDING);
    }

    @Transactional
    public void approveJoinRequest(Integer requestId) {
        authService.requireAdmin();
        Integer groupId = GroupContext.getCurrentGroupId();

        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Join request not found"));

        if (!request.getSquad().getId().equals(groupId)) {
            throw new SecurityException("This request does not belong to your squad");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        // Create player for the new member
        Player player = new Player();
        player.setName(request.getPlayerName());
        player.setSurname(request.getPlayerSurname());
        player.setPosition(request.getPlayerPosition());
        player.setFoot(request.getPlayerFoot());
        player.setSquad(request.getSquad());
        player = playerRepository.save(player);

        // Create membership
        GroupMembership membership = new GroupMembership();
        membership.setSquad(request.getSquad());
        membership.setUser(request.getUser());
        membership.setPlayer(player);
        membership.setRole(GroupRole.MEMBER);

        groupMembershipRepository.save(membership);

        // Update request status
        Integer reviewerId = authService.getCurrentUserId();
        User reviewer = userRepository.findById(reviewerId).orElse(null);
        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedByUser(reviewer);
        joinRequestRepository.save(request);
    }

    @Transactional
    public void rejectJoinRequest(Integer requestId) {
        authService.requireAdmin();
        Integer groupId = GroupContext.getCurrentGroupId();

        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Join request not found"));

        if (!request.getSquad().getId().equals(groupId)) {
            throw new SecurityException("This request does not belong to your squad");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        Integer reviewerId = authService.getCurrentUserId();
        User reviewer = userRepository.findById(reviewerId).orElse(null);
        request.setStatus(RequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedByUser(reviewer);
        joinRequestRepository.save(request);
    }

    @Transactional
    public void removeMember(Integer userId) {
        authService.requireAdmin();
        Integer groupId = GroupContext.getCurrentGroupId();

        Integer currentUserId = authService.getCurrentUserId();
        if (userId.equals(currentUserId)) {
            throw new IllegalStateException("You cannot remove yourself from the squad");
        }

        GroupMembership membership = groupMembershipRepository.findBySquadIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // Prevent removing the last admin
        if (membership.getRole() == GroupRole.ADMIN) {
            long adminCount = groupMembershipRepository.countBySquadIdAndRole(groupId, GroupRole.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot remove the last admin of the group");
            }
        }

        groupMembershipRepository.delete(membership);
    }

    @Transactional
    public void updateMemberRole(Integer userId, GroupRole newRole) {
        authService.requireAdmin();
        Integer groupId = GroupContext.getCurrentGroupId();

        GroupMembership membership = groupMembershipRepository.findBySquadIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // Prevent demoting the last admin
        if (membership.getRole() == GroupRole.ADMIN && newRole == GroupRole.MEMBER) {
            long adminCount = groupMembershipRepository.countBySquadIdAndRole(groupId, GroupRole.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot demote the last admin of the group");
            }
        }

        membership.setRole(newRole);
        groupMembershipRepository.save(membership);
    }

    @Transactional
    public void updateSquadName(String name) {
        authService.requireAdmin();
        Squad squad = getCurrentSquad();
        squad.setName(name);
        squadRepository.save(squad);
    }

    @Transactional
    public String regenerateInviteCode() {
        authService.requireAdmin();
        Squad squad = getCurrentSquad();
        String newCode = generateInviteCode();
        squad.setInviteCode(newCode);
        squadRepository.save(squad);
        return newCode;
    }

    // ==================== Super Admin ====================

    public List<SquadRequest> getPendingSquadRequests() {
        authService.requireSuperAdmin();
        return squadRequestRepository.findByStatus(RequestStatus.PENDING);
    }

    @Transactional
    public void approveSquadRequest(Integer requestId) {
        authService.requireSuperAdmin();

        SquadRequest request = squadRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Squad request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        // Create the squad
        Squad squad = new Squad();
        squad.setName(request.getName());
        squad.setInviteCode(generateInviteCode());
        squad.setOwnerUser(request.getRequestedByUser());
        squad = squadRepository.save(squad);

        // Create player for the requester in this squad
        Player player = new Player();
        player.setName(request.getPlayerName());
        player.setSurname(request.getPlayerSurname());
        player.setPosition(request.getPlayerPosition());
        player.setFoot(request.getPlayerFoot());
        player.setSquad(squad);
        player = playerRepository.save(player);

        // Make the requester the admin of the new squad
        GroupMembership membership = new GroupMembership();
        membership.setSquad(squad);
        membership.setUser(request.getRequestedByUser());
        membership.setPlayer(player);
        membership.setRole(GroupRole.ADMIN);
        groupMembershipRepository.save(membership);

        // Kullanıcının diğer pending join request'lerini temizle
        joinRequestRepository.findByUserIdAndStatus(
                request.getRequestedByUser().getId(), RequestStatus.PENDING)
                .forEach(joinRequestRepository::delete);

        // Update request
        Integer reviewerId = authService.getCurrentUserId();
        User reviewer = userRepository.findById(reviewerId).orElse(null);
        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedByUser(reviewer);
        squadRequestRepository.save(request);
    }

    @Transactional
    public void rejectSquadRequest(Integer requestId) {
        authService.requireSuperAdmin();

        SquadRequest request = squadRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Squad request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        Integer reviewerId = authService.getCurrentUserId();
        User reviewer = userRepository.findById(reviewerId).orElse(null);
        request.setStatus(RequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedByUser(reviewer);
        squadRequestRepository.save(request);
    }

    public List<Squad> getAllSquads() {
        authService.requireSuperAdmin();
        return squadRepository.findAll();
    }

    public String getAdminUsernameForSquad(Integer squadId) {
        return groupMembershipRepository.findFirstBySquadIdAndRole(squadId, GroupRole.ADMIN)
                .map(m -> m.getUser().getUsername())
                .orElse(null);
    }

    public String getAdminPlayerNameForSquad(Integer squadId) {
        return groupMembershipRepository.findFirstBySquadIdAndRole(squadId, GroupRole.ADMIN)
                .map(m -> m.getPlayer().getName() + " " + m.getPlayer().getSurname())
                .orElse(null);
    }

    public int getMemberCountForSquad(Integer squadId) {
        return (int) groupMembershipRepository.countBySquadId(squadId);
    }

    @Transactional
    public void cancelJoinRequest(Integer requestId) {
        Integer userId = authService.getCurrentUserId();

        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Join request not found"));

        if (!request.getUser().getId().equals(userId)) {
            throw new SecurityException("You can only cancel your own requests");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be cancelled");
        }

        joinRequestRepository.delete(request);
    }

    @Transactional
    public void cancelSquadRequest(Integer requestId) {
        Integer userId = authService.getCurrentUserId();
        SquadRequest request = squadRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Squad request not found"));
        if (!request.getRequestedByUser().getId().equals(userId)) {
            throw new SecurityException("You can only cancel your own requests");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be cancelled");
        }
        squadRequestRepository.delete(request);
    }

    @Transactional
    public void deactivateSquad(Integer squadId) {
        authService.requireSuperAdmin();
        Squad squad = squadRepository.findById(squadId)
                .orElseThrow(() -> new IllegalArgumentException("Squad not found"));
        squad.setActive(false);
        squadRepository.save(squad);
    }

    // ==================== Helpers ====================

    public long getPendingJoinRequestCount() {
        Integer groupId = GroupContext.getCurrentGroupId();
        return joinRequestRepository.countBySquadIdAndStatus(groupId, RequestStatus.PENDING);
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
