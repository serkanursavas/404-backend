package com.squad.squad.service.impl;

import com.squad.squad.dto.admin.AdminDecisionDTO;
import com.squad.squad.dto.group.GroupCreateRequestDTO;
import com.squad.squad.dto.group.GroupRequestResponseDTO;
import com.squad.squad.dto.group.GroupResponseDTO;
import com.squad.squad.entity.*;
import com.squad.squad.exception.InvalidCredentialsException;
import com.squad.squad.repository.*;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.service.GroupService;
import com.squad.squad.service.TenantContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupRequestRepository groupRequestRepository;
    private final UserRepository userRepository;
    private final GroupMembershipRepository membershipRepository;
    private final TenantContextService tenantContextService;

    @Autowired
    public GroupServiceImpl(GroupRepository groupRepository,
                            GroupRequestRepository groupRequestRepository,
                            UserRepository userRepository,
                            GroupMembershipRepository membershipRepository,
                            TenantContextService tenantContextService) {
        this.groupRepository = groupRepository;
        this.groupRequestRepository = groupRequestRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.tenantContextService = tenantContextService;
    }

    @Override
    @Transactional
    public String createGroupRequest(GroupCreateRequestDTO request) {
        // Mevcut kullanıcı bilgisini al
        CustomUserDetails currentUser = getCurrentUser();

        // Validasyonlar
        if (groupRequestRepository.existsByGroupNameAndStatus(request.getGroupName(), GroupRequest.RequestStatus.PENDING)) {
            throw new IllegalArgumentException("Bu isimde bekleyen bir grup talebi zaten mevcut.");
        }

        if (groupRepository.existsByName(request.getGroupName())) {
            throw new IllegalArgumentException("Bu isimde bir grup zaten mevcut.");
        }

        // Kullanıcının son 30 günde çok fazla talep oluşturup oluşturmadığını kontrol et
        Long recentRequests = groupRequestRepository.countRecentRequestsByUser(currentUser.getId());
        if (recentRequests >= 3) { // Maximum 3 talep per 30 gün
            throw new IllegalArgumentException("Son 30 gün içinde çok fazla grup talebi oluşturdunuz. Lütfen bekleyin.");
        }

        // Intended admin kullanıcısının var olup olmadığını kontrol et
        if (!userRepository.existsById(request.getIntendedAdminUserId())) {
            throw new IllegalArgumentException("Belirtilen grup admin kullanıcısı bulunamadı.");
        }

        // Grup talebini oluştur
        GroupRequest groupRequest = new GroupRequest(
                request.getGroupName(),
                request.getGroupDescription(),
                currentUser.getId(),
                request.getIntendedAdminUserId()
        );

        groupRequestRepository.save(groupRequest);

        return "Grup oluşturma talebiniz başarıyla gönderildi. Super Admin onayını bekliyor.";
    }

    @Override
    public List<GroupRequestResponseDTO> getPendingGroupRequests() {
        // Sadece ADMIN yetkisi kontrolü
        CustomUserDetails currentUser = getCurrentUser();

        System.out.println("=== ROLE DEBUG ===");
        System.out.println("Current user: " + currentUser.getUsername());
        System.out.println("User role: " + currentUser.getRole());
        System.out.println("Authorities: " + currentUser.getAuthorities());
        System.out.println("================");

        if (!"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new InvalidCredentialsException("Bu işlem için admin yetkisi gerekli.");
        }

        List<GroupRequest> pendingRequests = groupRequestRepository.findByStatus(GroupRequest.RequestStatus.PENDING);

        return pendingRequests.stream().map(this::convertToGroupRequestResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String processGroupRequest(Integer requestId, AdminDecisionDTO decision) {
        // Sadece ADMIN yetkisi kontrolü
        CustomUserDetails currentUser = getCurrentUser();
        if (!"ROLE_ADMIN".equals(currentUser.getRole())) {
            throw new InvalidCredentialsException("Bu işlem için admin yetkisi gerekli.");
        }

        GroupRequest request = groupRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Grup talebi bulunamadı."));

        if (request.getStatus() != GroupRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("Bu talep zaten işlenmiş.");
        }

        if ("APPROVE".equalsIgnoreCase(decision.getDecision())) {
            // Grup talebini onayla ve grup oluştur
            request.setStatus(GroupRequest.RequestStatus.APPROVED);
            request.setProcessedAt(LocalDateTime.now());
            request.setProcessedBy(currentUser.getId());

            // Yeni grup oluştur
            Group newGroup = new Group(
                    request.getGroupName(),
                    request.getGroupDescription(),
                    request.getRequestedBy(),
                    request.getIntendedAdmin()
            );
            newGroup.setStatus(Group.GroupStatus.APPROVED);
            newGroup.setApprovedAt(LocalDateTime.now());
            newGroup.setApprovedBy(currentUser.getId());

            Group savedGroup = groupRepository.save(newGroup);
            groupRequestRepository.save(request);

            // KRİTİK: Intended admin'i gruba otomatik olarak ekle
            addIntendedAdminToGroup(request.getIntendedAdmin(), savedGroup.getId());

            return "Grup talebi onaylandı ve grup başarıyla oluşturuldu. Intended admin gruba otomatik olarak eklendi.";

        } else if ("REJECT".equalsIgnoreCase(decision.getDecision())) {
            // Grup talebini reddet
            request.setStatus(GroupRequest.RequestStatus.REJECTED);
            request.setProcessedAt(LocalDateTime.now());
            request.setProcessedBy(currentUser.getId());
            request.setRejectionReason(decision.getRejectionReason());

            groupRequestRepository.save(request);

            return "Grup talebi reddedildi.";
        } else {
            throw new IllegalArgumentException("Geçersiz karar. 'APPROVE' veya 'REJECT' olmalı.");
        }
    }

    /**
     * Intended admin'i yeni oluşturulan gruba otomatik olarak ekle
     */
    private void addIntendedAdminToGroup(Integer intendedAdminId, Integer newGroupId) {
        Integer originalTenant = tenantContextService.getCurrentTenantId();
        try {
            // Yeni grubun context'ine geç
            tenantContextService.setTenantContext(newGroupId);

            // 1. GroupMembership oluştur (APPROVED ve GROUP_ADMIN olarak)
            GroupMembership membership = new GroupMembership(intendedAdminId, newGroupId);
            membership.setStatus(GroupMembership.MembershipStatus.APPROVED);
            membership.setRole(GroupMembership.MembershipRole.GROUP_ADMIN); // KRİTİK: GROUP_ADMIN
            membership.setApprovedAt(LocalDateTime.now());
            membership.setApprovedBy(1); // System tarafından onaylandı

            // DOĞRUDAN KAYDET - processMembershipDecision çağırma
            membershipRepository.save(membership);

            System.out.println("Intended admin saved with role: " + membership.getRole());

            // 2. User'ın groupId'sini güncelle
            transferUserToGroup(intendedAdminId, newGroupId);

        } finally {
            tenantContextService.setTenantContext(originalTenant);
        }
    }

    /**
     * Kullanıcıyı yeni gruba transfer et
     */
    private void transferUserToGroup(Integer userId, Integer newGroupId) {
        // Önce kullanıcıyı mevcut context'de bul
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            // Farklı context'lerde ara
            Integer originalTenant = tenantContextService.getCurrentTenantId();
            try {
                // Super admin context'inde ara
                tenantContextService.setSuperAdminContext();
                user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı."));
            } finally {
                tenantContextService.setTenantContext(originalTenant);
            }
        }

        Integer oldGroupId = user.getGroupId();

        // Kullanıcının grup ID'sini güncelle
        if (!oldGroupId.equals(newGroupId)) {
            Integer originalTenant = tenantContextService.getCurrentTenantId();
            try {
                // Eski context'de güncelle
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
    public List<GroupResponseDTO> getApprovedGroups() {
        List<Group> approvedGroups = groupRepository.findApprovedGroups();

        return approvedGroups.stream().map(this::convertToGroupResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<GroupRequestResponseDTO> getUserGroupRequests(Integer userId) {
        List<GroupRequest> userRequests = groupRequestRepository.findByRequestedBy(userId);

        return userRequests.stream().map(this::convertToGroupRequestResponseDTO).collect(Collectors.toList());
    }

    // Helper Methods
    private CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private GroupRequestResponseDTO convertToGroupRequestResponseDTO(GroupRequest request) {
        GroupRequestResponseDTO dto = new GroupRequestResponseDTO();
        dto.setId(request.getId());
        dto.setGroupName(request.getGroupName());
        dto.setGroupDescription(request.getGroupDescription());
        dto.setStatus(request.getStatus().toString());
        dto.setRequestedAt(request.getRequestedAt());

        // Username'leri getir
        User requestedByUser = userRepository.findById(request.getRequestedBy()).orElse(null);
        if (requestedByUser != null) {
            dto.setRequestedByUsername(requestedByUser.getUsername());
        }

        User intendedAdminUser = userRepository.findById(request.getIntendedAdmin()).orElse(null);
        if (intendedAdminUser != null) {
            dto.setIntendedAdminUsername(intendedAdminUser.getUsername());
        }

        return dto;
    }

    private GroupResponseDTO convertToGroupResponseDTO(Group group) {
        GroupResponseDTO dto = new GroupResponseDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setStatus(group.getStatus().toString());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setApprovedAt(group.getApprovedAt());

        // Username'leri getir
        User createdByUser = userRepository.findById(group.getCreatedBy()).orElse(null);
        if (createdByUser != null) {
            dto.setCreatedByUsername(createdByUser.getUsername());
        }

        User groupAdminUser = userRepository.findById(group.getGroupAdmin()).orElse(null);
        if (groupAdminUser != null) {
            dto.setGroupAdminUsername(groupAdminUser.getUsername());
        }

        return dto;
    }
}