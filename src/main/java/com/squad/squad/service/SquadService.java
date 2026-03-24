package com.squad.squad.service;

import com.squad.squad.context.GroupContext;
import com.squad.squad.dto.squad.*;
import com.squad.squad.entity.*;
import com.squad.squad.enums.GroupRole;
import com.squad.squad.enums.RequestStatus;
import com.squad.squad.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @Transactional
    public List<SquadSummaryDTO> getMySquads() {
        Integer userId = authService.getCurrentUserId();
        List<GroupMembership> memberships = groupMembershipRepository.findByUserId(userId);
        return memberships.stream()
                .map(m -> {
                    Integer squadId = m.getSquad().getId();
                    String adminName = getAdminPlayerNameForSquad(squadId);
                    int memberCount = getMemberCountForSquad(squadId);
                    return new SquadSummaryDTO(squadId, m.getSquad().getName(), m.getRole().name(), adminName, memberCount);
                })
                .toList();
    }

    @Transactional
    public Map<String, Object> getMyRequests() {
        Integer userId = authService.getCurrentUserId();
        List<SquadRequest> squadRequests = squadRequestRepository.findByRequestedByUserId(userId);
        List<JoinRequest> joinRequests = joinRequestRepository.findByUserId(userId);

        List<SquadRequestDTO> squadRequestDTOs = squadRequests.stream()
                .map(r -> {
                    SquadRequestDTO dto = new SquadRequestDTO();
                    dto.setId(r.getId());
                    dto.setSquadName(r.getName());
                    dto.setStatus(r.getStatus().name());
                    dto.setCreatedAt(r.getCreatedAt());
                    return dto;
                }).toList();

        List<JoinRequestDTO> joinRequestDTOs = joinRequests.stream()
                .map(r -> {
                    JoinRequestDTO dto = new JoinRequestDTO();
                    dto.setId(r.getId());
                    dto.setSquadName(r.getSquad().getName());
                    dto.setAdminUsername(getAdminUsernameForSquad(r.getSquad().getId()));
                    dto.setPlayerName(r.getPlayerName());
                    dto.setPlayerSurname(r.getPlayerSurname());
                    dto.setStatus(r.getStatus().name());
                    dto.setCreatedAt(r.getCreatedAt());
                    return dto;
                }).toList();

        int pendingCount = (int) squadRequests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count()
                + (int) joinRequests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count();

        return Map.of(
                "squadRequests", squadRequestDTOs,
                "joinRequests", joinRequestDTOs,
                "pendingCount", pendingCount
        );
    }

    // ==================== Group Admin (group context required) ====================

    @Transactional
    public SquadDetailDTO getCurrentSquad() {
        Integer groupId = GroupContext.getCurrentGroupId();
        Squad squad = squadRepository.findById(groupId)
                .orElseThrow(() -> new IllegalStateException("Squad not found"));
        SquadDetailDTO dto = new SquadDetailDTO();
        dto.setId(squad.getId());
        dto.setName(squad.getName());
        dto.setInviteCode(squad.getInviteCode());
        dto.setCreatedAt(squad.getCreatedAt());
        dto.setMemberCount(getMemberCountForSquad(squad.getId()));
        return dto;
    }

    @Transactional
    public List<MemberDTO> getMembers() {
        authService.requireAdmin();
        Integer groupId = GroupContext.getCurrentGroupId();
        List<GroupMembership> memberships = groupMembershipRepository.findBySquadId(groupId);
        return memberships.stream()
                .map(m -> {
                    MemberDTO dto = new MemberDTO();
                    dto.setUserId(m.getUser().getId());
                    dto.setUsername(m.getUser().getUsername());
                    dto.setPlayerId(m.getPlayer().getId());
                    dto.setPlayerName(m.getPlayer().getName());
                    dto.setPlayerSurname(m.getPlayer().getSurname());
                    dto.setRole(m.getRole().name());
                    dto.setPlayerPosition(m.getPlayer().getPosition());
                    dto.setJoinedAt(m.getJoinedAt());
                    return dto;
                }).toList();
    }

    @Transactional
    public List<JoinRequestDTO> getPendingJoinRequests() {
        authService.requireAdmin();
        Integer groupId = GroupContext.getCurrentGroupId();
        List<JoinRequest> requests = joinRequestRepository.findBySquadIdAndStatus(groupId, RequestStatus.PENDING);
        return requests.stream()
                .map(r -> {
                    JoinRequestDTO dto = new JoinRequestDTO();
                    dto.setId(r.getId());
                    dto.setUsername(r.getUser().getUsername());
                    dto.setPlayerName(r.getPlayerName());
                    dto.setPlayerSurname(r.getPlayerSurname());
                    dto.setPlayerPosition(r.getPlayerPosition());
                    dto.setPlayerFoot(r.getPlayerFoot());
                    dto.setStatus(r.getStatus().name());
                    dto.setCreatedAt(r.getCreatedAt());
                    return dto;
                }).toList();
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
        Integer currentUserId = authService.getCurrentUserId();
        if (userId.equals(currentUserId)) {
            throw new IllegalStateException("You cannot change your own role");
        }
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
        Squad squad = findCurrentSquadEntity();
        squad.setName(name);
        squadRepository.save(squad);
    }

    @Transactional
    public String regenerateInviteCode() {
        authService.requireAdmin();
        Squad squad = findCurrentSquadEntity();
        String newCode = generateInviteCode();
        squad.setInviteCode(newCode);
        squadRepository.save(squad);
        return newCode;
    }

    // ==================== Super Admin ====================

    @Transactional
    public List<SquadRequestDTO> getPendingSquadRequests() {
        authService.requireSuperAdmin();
        List<SquadRequest> requests = squadRequestRepository.findByStatus(RequestStatus.PENDING);
        return requests.stream()
                .map(r -> {
                    SquadRequestDTO dto = new SquadRequestDTO();
                    dto.setId(r.getId());
                    dto.setSquadName(r.getName());
                    dto.setRequestedByUsername(r.getRequestedByUser().getUsername());
                    dto.setPlayerName(r.getPlayerName());
                    dto.setPlayerSurname(r.getPlayerSurname());
                    dto.setStatus(r.getStatus().name());
                    dto.setCreatedAt(r.getCreatedAt());
                    return dto;
                }).toList();
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

    @Transactional
    public List<SquadDetailDTO> getAllSquads() {
        authService.requireSuperAdmin();
        List<Squad> squads = squadRepository.findAll();
        return squads.stream()
                .map(s -> {
                    SquadDetailDTO dto = new SquadDetailDTO();
                    dto.setId(s.getId());
                    dto.setName(s.getName());
                    dto.setInviteCode(s.getInviteCode());
                    dto.setCreatedAt(s.getCreatedAt());
                    dto.setMemberCount(getMemberCountForSquad(s.getId()));
                    dto.setActive(s.isActive());
                    return dto;
                }).toList();
    }

    @Transactional
    public String getAdminUsernameForSquad(Integer squadId) {
        return groupMembershipRepository.findFirstBySquadIdAndRole(squadId, GroupRole.ADMIN)
                .map(m -> m.getUser().getUsername())
                .orElse(null);
    }

    @Transactional
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

    public Integer getMyPlayerId() {
        try {
            return authService.getCurrentPlayerId();
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== Helpers ====================

    public long getPendingJoinRequestCount() {
        Integer groupId = GroupContext.getCurrentGroupId();
        return joinRequestRepository.countBySquadIdAndStatus(groupId, RequestStatus.PENDING);
    }

    private Squad findCurrentSquadEntity() {
        Integer groupId = GroupContext.getCurrentGroupId();
        return squadRepository.findById(groupId)
                .orElseThrow(() -> new IllegalStateException("Squad not found"));
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
