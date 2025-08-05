package com.squad.squad.service.impl;

import com.squad.squad.dto.admin.AdminDecisionDTO;
import com.squad.squad.dto.membership.MembershipRequestDTO;
import com.squad.squad.dto.membership.MembershipResponseDTO;
import com.squad.squad.entity.*;
import com.squad.squad.exception.InvalidCredentialsException;
import com.squad.squad.repository.*;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.service.MembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MembershipServiceImpl implements MembershipService {

    private final GroupMembershipRepository membershipRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public MembershipServiceImpl(GroupMembershipRepository membershipRepository,
            GroupRepository groupRepository,
            UserRepository userRepository) {
        this.membershipRepository = membershipRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public String requestMembership(MembershipRequestDTO request) {
        CustomUserDetails currentUser = getCurrentUser();

        // Grup var mı kontrol et - güvenlik kısıtlamalarını bypass et
        Group group = groupRepository.findGroupWithDetails(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Grup bulunamadı."));

        if (group.getStatus() != Group.GroupStatus.APPROVED) {
            throw new IllegalArgumentException("Bu grup henüz onaylanmamış.");
        }

        // Kullanıcının zaten bu gruba başvurusu var mı?
        if (membershipRepository.existsByUserIdAndGroupIdAndStatus(
                currentUser.getId(), request.getGroupId(), GroupMembership.MembershipStatus.PENDING)) {
            throw new IllegalArgumentException("Bu gruba zaten bekleyen bir başvurunuz var.");
        }

        // Kullanıcının zaten bu grupta aktif üyeliği var mı?
        if (membershipRepository.existsByUserIdAndGroupIdAndStatus(
                currentUser.getId(), request.getGroupId(), GroupMembership.MembershipStatus.APPROVED)) {
            throw new IllegalArgumentException("Bu grubun zaten üyesisiniz.");
        }

        // Yeni üyelik talebi oluştur
        GroupMembership membership = new GroupMembership(currentUser.getId(), request.getGroupId());
        membershipRepository.save(membership);

        return "Grup üyelik başvurunuz başarıyla gönderildi. Grup admin onayını bekliyor.";
    }

    @Override
    public List<MembershipResponseDTO> getPendingMemberships() {
        CustomUserDetails currentUser = getCurrentUser();

        // Super Admin kontrolü
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            return getAllPendingMembershipsForSuperAdmin();
        }

        // Normal Group Admin kontrolü
        return getPendingMembershipsForGroupAdmin(currentUser);
    }

    /**
     * Super Admin için tüm bekleyen üyelik taleplerini getir
     */
    private List<MembershipResponseDTO> getAllPendingMembershipsForSuperAdmin() {
        List<GroupMembership> pendingMemberships = membershipRepository
                .findByStatus(GroupMembership.MembershipStatus.PENDING);
        return pendingMemberships.stream()
                .map(this::convertToMembershipResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Group Admin için sadece kendi grubundaki bekleyen üyelik taleplerini getir
     */
    private List<MembershipResponseDTO> getPendingMembershipsForGroupAdmin(CustomUserDetails currentUser) {
        // Kullanıcının grup admin olduğu grupları bul
        List<GroupMembership> adminMemberships = membershipRepository.findByUserIdAndRoleAndStatus(
                currentUser.getId(), GroupMembership.MembershipRole.GROUP_ADMIN,
                GroupMembership.MembershipStatus.APPROVED);

        List<MembershipResponseDTO> allPendingMemberships = new ArrayList<>();

        for (GroupMembership adminMembership : adminMemberships) {
            // Her grup için bekleyen üyelik taleplerini getir
            List<GroupMembership> groupPendingMemberships = membershipRepository.findByGroupIdAndStatus(
                    adminMembership.getGroupId(), GroupMembership.MembershipStatus.PENDING);

            List<MembershipResponseDTO> groupMemberships = groupPendingMemberships.stream()
                    .map(this::convertToMembershipResponseDTO)
                    .collect(Collectors.toList());

            allPendingMemberships.addAll(groupMemberships);
        }

        return allPendingMemberships;
    }

    @Override
    @Transactional
    public String processMembershipRequest(Integer membershipId, AdminDecisionDTO decision) {
        CustomUserDetails currentUser = getCurrentUser();

        // Super Admin kontrolü
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            return processMembershipForSuperAdmin(membershipId, decision, currentUser);
        }

        // Normal Group Admin kontrolü
        return processMembershipForGroupAdmin(membershipId, decision, currentUser);
    }

    private String processMembershipForSuperAdmin(Integer membershipId, AdminDecisionDTO decision,
            CustomUserDetails currentUser) {
        // Super Admin tüm üyelik taleplerini işleyebilir
        GroupMembership membership = membershipRepository.findGroupMembershipWithDetails(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Üyelik talebi bulunamadı."));

        return processMembershipDecision(membership, decision, currentUser);
    }

    private String processMembershipForGroupAdmin(Integer membershipId, AdminDecisionDTO decision,
            CustomUserDetails currentUser) {
        // Group Admin sadece kendi grubundaki üyelik taleplerini işleyebilir
        GroupMembership membership = membershipRepository.findGroupMembershipWithDetails(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Üyelik talebi bulunamadı."));

        // Kullanıcının bu grubun admin'i olup olmadığını kontrol et
        boolean isAdminOfGroup = membershipRepository.existsByUserIdAndGroupIdAndRoleAndStatus(
                currentUser.getId(), membership.getGroupId(), GroupMembership.MembershipRole.GROUP_ADMIN,
                GroupMembership.MembershipStatus.APPROVED);

        if (!isAdminOfGroup) {
            throw new InvalidCredentialsException("Bu üyelik talebini işlemek için yetkiniz yok.");
        }

        return processMembershipDecision(membership, decision, currentUser);
    }

    private String processMembershipDecision(GroupMembership membership, AdminDecisionDTO decision,
            CustomUserDetails currentUser) {
        if (membership.getStatus() != GroupMembership.MembershipStatus.PENDING) {
            throw new IllegalArgumentException("Bu üyelik talebi zaten işlenmiş.");
        }

        if ("APPROVE".equals(decision.getDecision())) {
            membership.setStatus(GroupMembership.MembershipStatus.APPROVED);
            membership.setApprovedAt(LocalDateTime.now());
            membership.setApprovedBy(currentUser.getId());

            // Kullanıcıyı gruba transfer et
            transferUserToGroup(membership.getUserId(), membership.getGroupId());

            membershipRepository.save(membership);
            return "Üyelik talebi onaylandı.";
        } else if ("REJECT".equals(decision.getDecision())) {
            membership.setStatus(GroupMembership.MembershipStatus.REJECTED);
            membership.setApprovedAt(LocalDateTime.now());
            membership.setApprovedBy(currentUser.getId());

            membershipRepository.save(membership);
            return "Üyelik talebi reddedildi.";
        } else {
            throw new IllegalArgumentException("Geçersiz karar: " + decision.getDecision());
        }
    }

    private void transferUserToGroup(Integer userId, Integer newGroupId) {
        User user = userRepository.findUserWithDetails(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı."));

        Integer oldGroupId = user.getGroupId();

        // Kullanıcının grup ID'sini güncelle
        if (oldGroupId == null || !oldGroupId.equals(newGroupId)) {
            user.setGroupId(newGroupId);
            userRepository.save(user);

            // Player'ı da transfer et
            if (user.getPlayer() != null) {
                user.getPlayer().setGroupId(newGroupId);
            }
        } else {
            // Aynı context'de güncelle
            user.setGroupId(newGroupId);
            userRepository.save(user);

            if (user.getPlayer() != null) {
                user.getPlayer().setGroupId(newGroupId);
            }
        }
    }

    @Override
    public List<MembershipResponseDTO> getUserMemberships(Integer userId) {
        List<GroupMembership> memberships = membershipRepository.findByUserId(userId);
        return memberships.stream()
                .map(this::convertToMembershipResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembershipResponseDTO> getGroupMembers(Integer groupId) {
        List<GroupMembership> memberships = membershipRepository.findByGroupIdAndStatus(
                groupId, GroupMembership.MembershipStatus.APPROVED);
        return memberships.stream()
                .map(this::convertToMembershipResponseDTO)
                .collect(Collectors.toList());
    }

    // Helper Methods
    private CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private MembershipResponseDTO convertToMembershipResponseDTO(GroupMembership membership) {
        MembershipResponseDTO dto = new MembershipResponseDTO();
        dto.setId(membership.getId());
        dto.setUserId(membership.getUserId());
        dto.setGroupId(membership.getGroupId());
        dto.setStatus(membership.getStatus().toString());
        dto.setRole(membership.getRole().toString());
        dto.setRequestedAt(membership.getRequestedAt());
        dto.setApprovedAt(membership.getApprovedAt());
        dto.setApprovedBy(membership.getApprovedBy());

        // User bilgilerini ekle
        Optional<User> user = userRepository.findUserWithDetails(membership.getUserId());
        if (user.isPresent()) {
            dto.setUsername(user.get().getUsername());
            if (user.get().getPlayer() != null) {
                dto.setPlayerName(user.get().getPlayer().getName());
            }
        }

        // Group bilgilerini ekle
        Optional<Group> group = groupRepository.findGroupWithDetails(membership.getGroupId());
        if (group.isPresent()) {
            dto.setGroupName(group.get().getName());
        }

        return dto;
    }

    @Override
    public boolean isUserGroupAdmin(Object userPrincipal) {
        if (!(userPrincipal instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) userPrincipal;
        return membershipRepository.existsByUserIdAndRoleAndStatus(
                userDetails.getId(), GroupMembership.MembershipRole.GROUP_ADMIN,
                GroupMembership.MembershipStatus.APPROVED);
    }

    @Override
    public boolean isUserAdminOfGroup(Object userPrincipal, Integer groupId) {
        if (!(userPrincipal instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) userPrincipal;
        return membershipRepository.existsByUserIdAndGroupIdAndRoleAndStatus(
                userDetails.getId(), groupId, GroupMembership.MembershipRole.GROUP_ADMIN,
                GroupMembership.MembershipStatus.APPROVED);
    }

    @Override
    public boolean canUserProcessMembership(Object userPrincipal, Integer membershipId) {
        if (!(userPrincipal instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) userPrincipal;

        // Super Admin her şeyi yapabilir
        if ("ROLE_ADMIN".equals(userDetails.getRole())) {
            return true;
        }

        // Group Admin sadece kendi grubundaki üyelik taleplerini işleyebilir
        Optional<GroupMembership> membership = membershipRepository.findGroupMembershipWithDetails(membershipId);
        if (membership.isPresent()) {
            return isUserAdminOfGroup(userPrincipal, membership.get().getGroupId());
        }

        return false;
    }
}