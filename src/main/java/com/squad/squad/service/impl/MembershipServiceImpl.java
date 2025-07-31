package com.squad.squad.service.impl;

import com.squad.squad.dto.admin.AdminDecisionDTO;
import com.squad.squad.dto.membership.MembershipRequestDTO;
import com.squad.squad.dto.membership.MembershipResponseDTO;
import com.squad.squad.entity.*;
import com.squad.squad.exception.InvalidCredentialsException;
import com.squad.squad.repository.*;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.service.MembershipService;
import com.squad.squad.service.TenantContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MembershipServiceImpl implements MembershipService {

    private final GroupMembershipRepository membershipRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final TenantContextService tenantContextService;

    @Autowired
    public MembershipServiceImpl(GroupMembershipRepository membershipRepository,
            GroupRepository groupRepository,
            UserRepository userRepository,
            TenantContextService tenantContextService) {
        this.membershipRepository = membershipRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.tenantContextService = tenantContextService;
    }

    @Override
    @Transactional
    public String requestMembership(MembershipRequestDTO request) {
        CustomUserDetails currentUser = getCurrentUser();

        // Grup var mı kontrol et
        Group group = groupRepository.findById(request.getGroupId())
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

        // Yeni üyelik talebi oluştur - hedef grubun context'inde
        Integer originalTenant = tenantContextService.getCurrentTenantId();
        try {
            tenantContextService.setTenantContext(request.getGroupId());

            GroupMembership membership = new GroupMembership(currentUser.getId(), request.getGroupId());
            membershipRepository.save(membership);

            return "Grup üyelik başvurunuz başarıyla gönderildi. Grup admin onayını bekliyor.";
        } finally {
            tenantContextService.setTenantContext(originalTenant);
        }
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
        // Super Admin için RLS bypass - tenant context'i null yap
        Integer originalTenant = tenantContextService.getCurrentTenantId();
        try {
            // tenantContextService.clearTenantContext();
            tenantContextService.setSuperAdminContext(); // Super Admin context'i set et

            // Tüm bekleyen üyelik taleplerini getir
            List<GroupMembership> allPendingMemberships = membershipRepository.findByStatusNative("PENDING");

            return allPendingMemberships.stream()
                    .map(this::convertToMembershipResponseDTO)
                    .collect(Collectors.toList());
        } finally {
            tenantContextService.setTenantContext(originalTenant);
        }
    }

    /**
     * Group Admin için kendi gruplarındaki bekleyen üyelik taleplerini getir
     */
    private List<MembershipResponseDTO> getPendingMembershipsForGroupAdmin(CustomUserDetails currentUser) {
        // Kullanıcının GROUP_ADMIN rolüne sahip olduğu grupları bul
        List<GroupMembership> groupAdminMemberships = membershipRepository.findByUserIdAndRoleAndStatus(
                currentUser.getId(), GroupMembership.MembershipRole.GROUP_ADMIN,
                GroupMembership.MembershipStatus.APPROVED);

        if (groupAdminMemberships.isEmpty()) {
            throw new InvalidCredentialsException("Hiçbir grubun admini değilsiniz.");
        }

        List<MembershipResponseDTO> allPendingMemberships = new ArrayList<>();

        // Her grup için ayrı ayrı context değiştirerek sorgula
        for (GroupMembership adminMembership : groupAdminMemberships) {
            Integer originalTenant = tenantContextService.getCurrentTenantId();
            try {
                tenantContextService.setTenantContext(adminMembership.getGroupId());

                List<GroupMembership> groupPendingMemberships = membershipRepository
                        .findPendingMembershipsByGroupId(adminMembership.getGroupId());

                List<MembershipResponseDTO> groupMemberships = groupPendingMemberships.stream()
                        .map(this::convertToMembershipResponseDTO)
                        .collect(Collectors.toList());

                allPendingMemberships.addAll(groupMemberships);

            } finally {
                tenantContextService.setTenantContext(originalTenant);
            }
        }

        return allPendingMemberships;
    }

    @Override
    @Transactional
    public String processMembershipRequest(Integer membershipId, AdminDecisionDTO decision) {
        CustomUserDetails currentUser = getCurrentUser();

        // Super Admin ise direkt işleme al
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            return processMembershipForSuperAdmin(membershipId, decision, currentUser);
        }

        // Group Admin kontrolü
        return processMembershipForGroupAdmin(membershipId, decision, currentUser);
    }

    /**
     * Super Admin için üyelik talebini işle
     */
    private String processMembershipForSuperAdmin(Integer membershipId, AdminDecisionDTO decision,
            CustomUserDetails currentUser) {
        Integer originalTenant = tenantContextService.getCurrentTenantId();
        try {
            // tenantContextService.clearTenantContext(); // RLS bypass
            tenantContextService.setSuperAdminContext(); // Super Admin context'i set et

            GroupMembership membership = membershipRepository.findById(membershipId)
                    .orElseThrow(() -> new IllegalArgumentException("Üyelik talebi bulunamadı."));

            if (membership.getStatus() != GroupMembership.MembershipStatus.PENDING) {
                throw new IllegalArgumentException("Bu talep zaten işlenmiş.");
            }

            return processMembershipDecision(membership, decision, currentUser);

        } finally {
            tenantContextService.setTenantContext(originalTenant);
        }
    }

    /**
     * Group Admin için üyelik talebini işle
     */
    private String processMembershipForGroupAdmin(Integer membershipId, AdminDecisionDTO decision,
            CustomUserDetails currentUser) {
        // Önce membership'i bul
        GroupMembership membership = null;
        Integer targetGroupId = null;

        // Kullanıcının GROUP_ADMIN rolüne sahip olduğu grupları bul
        List<GroupMembership> adminMemberships = membershipRepository.findByUserIdAndStatusAndRole(
                currentUser.getId(), GroupMembership.MembershipStatus.APPROVED, GroupMembership.MembershipRole.GROUP_ADMIN);

        if (adminMemberships.isEmpty()) {
            throw new InvalidCredentialsException("Hiçbir grubun admini değilsiniz.");
        }

        // Membership'i admin olduğu gruplar arasında ara
        for (GroupMembership adminMembership : adminMemberships) {
            Integer originalTenant = tenantContextService.getCurrentTenantId();
            try {
                tenantContextService.setTenantContext(adminMembership.getGroupId());

                GroupMembership tempMembership = membershipRepository.findById(membershipId).orElse(null);
                if (tempMembership != null && tempMembership.getGroupId().equals(adminMembership.getGroupId())) {
                    membership = tempMembership;
                    targetGroupId = adminMembership.getGroupId();
                    break;
                }

            } finally {
                tenantContextService.setTenantContext(originalTenant);
            }
        }

        if (membership == null) {
            throw new InvalidCredentialsException("Bu üyelik talebi için yetkiniz yok.");
        }

        if (membership.getStatus() != GroupMembership.MembershipStatus.PENDING) {
            throw new IllegalArgumentException("Bu talep zaten işlenmiş.");
        }

        // İşlemi hedef grubun context'inde yap
        Integer originalTenant = tenantContextService.getCurrentTenantId();
        try {
            tenantContextService.setTenantContext(targetGroupId);
            return processMembershipDecision(membership, decision, currentUser);
        } finally {
            tenantContextService.setTenantContext(originalTenant);
        }
    }

    /**
     * Membership kararını işle (ortak method)
     */
    private String processMembershipDecision(GroupMembership membership, AdminDecisionDTO decision,
            CustomUserDetails currentUser) {
        if ("APPROVE".equalsIgnoreCase(decision.getDecision())) {
            membership.setStatus(GroupMembership.MembershipStatus.APPROVED);
            membership.setApprovedAt(LocalDateTime.now());
            membership.setApprovedBy(currentUser.getId());

            // Normal üyelik başvuruları her zaman MEMBER rolü alır
            // GROUP_ADMIN rolü sadece grup oluşturulurken sistem tarafından atanır
            membership.setRole(GroupMembership.MembershipRole.MEMBER);

            membershipRepository.save(membership);

            // KRİTİK: User'ı da gruba transfer et
            transferUserToGroup(membership.getUserId(), membership.getGroupId());

            return "Üyelik talebi onaylandı ve kullanıcı gruba transfer edildi.";

        } else if ("REJECT".equalsIgnoreCase(decision.getDecision())) {
            membership.setStatus(GroupMembership.MembershipStatus.REJECTED);
            membership.setApprovedAt(LocalDateTime.now());
            membership.setApprovedBy(currentUser.getId());

            membershipRepository.save(membership);
            return "Üyelik talebi reddedildi.";

        } else {
            throw new IllegalArgumentException("Geçersiz karar. 'APPROVE' veya 'REJECT' olmalı.");
        }
    }

    /**
     * Kullanıcıyı yeni gruba transfer et
     */
    private void transferUserToGroup(Integer userId, Integer newGroupId) {
        // Kullanıcıyı eski context'de bul
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı."));

        Integer oldGroupId = user.getGroupId();

        // Eğer kullanıcı farklı bir context'deyse, onu getir
        if (!oldGroupId.equals(newGroupId)) {
            Integer originalTenant = tenantContextService.getCurrentTenantId();
            try {
                // Önce eski context'de güncelle
                tenantContextService.setTenantContext(oldGroupId);
                user.setGroupId(newGroupId);
                userRepository.save(user);

                // Player'ı da transfer et
                if (user.getPlayer() != null) {
                    user.getPlayer().setGroupId(newGroupId);
                }

            } finally {
                tenantContextService.setTenantContext(originalTenant);
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
        CustomUserDetails currentUser = getCurrentUser();

        // Super Admin kontrolü
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            Integer originalTenant = tenantContextService.getCurrentTenantId();
            try {
                tenantContextService.setTenantContext(groupId);

                List<GroupMembership> members = membershipRepository.findByGroupIdAndStatus(
                        groupId, GroupMembership.MembershipStatus.APPROVED);

                return members.stream()
                        .map(this::convertToMembershipResponseDTO)
                        .collect(Collectors.toList());

            } finally {
                tenantContextService.setTenantContext(originalTenant);
            }
        }

        // Group Admin kontrolü
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grup bulunamadı."));

        if (!group.getGroupAdmin().equals(currentUser.getId())) {
            throw new InvalidCredentialsException("Bu grup için yetkiniz yok.");
        }

        List<GroupMembership> members = membershipRepository.findByGroupIdAndStatus(
                groupId, GroupMembership.MembershipStatus.APPROVED);

        return members.stream()
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

        // User bilgilerini getir - cross-context access için özel handling
        Integer originalTenant = tenantContextService.getCurrentTenantId();
        try {
            // User'ın asıl grubunda ara
            User user = null;

            // Önce mevcut context'de dene
            user = userRepository.findById(membership.getUserId()).orElse(null);

            // Bulamazsa, membership'in grup context'inde dene
            if (user == null) {
                tenantContextService.setTenantContext(membership.getGroupId());
                user = userRepository.findById(membership.getUserId()).orElse(null);
            }

            if (user != null) {
                dto.setUsername(user.getUsername());
                if (user.getPlayer() != null) {
                    dto.setPlayerName(user.getPlayer().getName() + " " + user.getPlayer().getSurname());
                }
            }
        } finally {
            tenantContextService.setTenantContext(originalTenant);
        }

        // Grup bilgilerini getir
        Group group = groupRepository.findById(membership.getGroupId()).orElse(null);
        if (group != null) {
            dto.setGroupName(group.getName());
        }

        return dto;
    }

    // Security check methods for PreAuthorize annotations
    @Override
    public boolean isUserGroupAdmin(Object userPrincipal) {
        if (!(userPrincipal instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails user = (CustomUserDetails) userPrincipal;

        // Super Admin her zaman erişebilir
        if ("ROLE_ADMIN".equals(user.getRole())) {
            return true;
        }

        // Kullanıcının GROUP_ADMIN rolüne sahip olduğu grupları kontrol et
        List<GroupMembership> adminMemberships = membershipRepository.findByUserIdAndStatusAndRole(
                user.getId(), GroupMembership.MembershipStatus.APPROVED, GroupMembership.MembershipRole.GROUP_ADMIN);

        return !adminMemberships.isEmpty();
    }

    @Override
    public boolean isUserAdminOfGroup(Object userPrincipal, Integer groupId) {
        if (!(userPrincipal instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails user = (CustomUserDetails) userPrincipal;

        // Super Admin her zaman erişebilir
        if ("ROLE_ADMIN".equals(user.getRole())) {
            return true;
        }

        // Belirli grubun adminliğini kontrol et
        Integer originalTenant = tenantContextService.getCurrentTenantId();
        try {
            tenantContextService.setTenantContext(groupId);

            // Grup admin kontrolü - groups tablosundan
            Group group = groupRepository.findById(groupId).orElse(null);
            if (group != null && group.getGroupAdmin().equals(user.getId())) {
                return true;
            }

            // Membership üzerinden admin kontrolü
            return membershipRepository.existsByUserIdAndGroupIdAndStatusAndRole(
                    user.getId(), groupId, GroupMembership.MembershipStatus.APPROVED, 
                    GroupMembership.MembershipRole.GROUP_ADMIN);

        } finally {
            tenantContextService.setTenantContext(originalTenant);
        }
    }

    @Override
    public boolean canUserProcessMembership(Object userPrincipal, Integer membershipId) {
        if (!(userPrincipal instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails user = (CustomUserDetails) userPrincipal;

        // Super Admin her zaman erişebilir
        if ("ROLE_ADMIN".equals(user.getRole())) {
            return true;
        }

        // Membership'i bulup hangi gruba ait olduğunu kontrol et
        Integer originalTenant = tenantContextService.getCurrentTenantId();
        try {
            // Önce kullanıcının admin olduğu grupları bul
            List<GroupMembership> adminMemberships = membershipRepository.findByUserIdAndStatusAndRole(
                    user.getId(), GroupMembership.MembershipStatus.APPROVED, GroupMembership.MembershipRole.GROUP_ADMIN);

            // Her admin grubunda membership'i ara
            for (GroupMembership adminMembership : adminMemberships) {
                tenantContextService.setTenantContext(adminMembership.getGroupId());

                GroupMembership targetMembership = membershipRepository.findById(membershipId).orElse(null);
                if (targetMembership != null && 
                    targetMembership.getGroupId().equals(adminMembership.getGroupId())) {
                    return true;
                }
            }

            return false;

        } finally {
            tenantContextService.setTenantContext(originalTenant);
        }
    }
}