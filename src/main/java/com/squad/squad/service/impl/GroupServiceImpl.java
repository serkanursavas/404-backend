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

    public GroupServiceImpl(GroupRepository groupRepository,
            GroupRequestRepository groupRequestRepository,
            UserRepository userRepository,
            GroupMembershipRepository membershipRepository) {
        this.groupRepository = groupRepository;
        this.groupRequestRepository = groupRequestRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;

    }

    @Override
    @Transactional
    public String createGroupRequest(GroupCreateRequestDTO request) {
        // Validasyonlar
        if (groupRequestRepository.existsByGroupNameAndStatus(request.getGroupName(),
                GroupRequest.RequestStatus.PENDING)) {
            throw new IllegalArgumentException("Bu isimde bekleyen bir grup talebi zaten mevcut.");
        }

        if (groupRepository.existsByName(request.getGroupName())) {
            throw new IllegalArgumentException("Bu isimde bir grup zaten mevcut.");
        }

        // Intended admin kullanıcısının var olup olmadığını kontrol et
        if (!userRepository.existsById(request.getIntendedAdminUserId())) {
            throw new IllegalArgumentException("Belirtilen grup admin kullanıcısı bulunamadı.");
        }

        // Grup talebini oluştur - intendedAdminUserId'yi requestedBy olarak kullan
        GroupRequest groupRequest = new GroupRequest(
                request.getGroupName(),
                request.getGroupDescription(),
                request.getIntendedAdminUserId(), // requestedBy olarak intended admin'i kullan
                request.getIntendedAdminUserId());

        groupRequestRepository.save(groupRequest);

        return "Grup oluşturma talebiniz başarıyla gönderildi. Super Admin onayını bekliyor.";
    }

    @Override
    public List<GroupRequestResponseDTO> getPendingGroupRequests() {
        // Sadece user id 1 olan kullanıcı görebilsin
        CustomUserDetails currentUser = getCurrentUser();

        if (currentUser.getId() != 1) {
            throw new InvalidCredentialsException("Bu işlem için admin yetkisi gerekli.");
        }

        List<GroupRequest> pendingRequests = groupRequestRepository.findByStatus("PENDING");

        return pendingRequests.stream().map(this::convertToGroupRequestResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String processGroupRequest(Integer requestId, AdminDecisionDTO decision) {
        // Sadece user id 1 olan kullanıcı görebilsin
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser.getId() != 1) {
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
                    request.getIntendedAdmin(), // createdBy = intended admin
                    request.getIntendedAdmin()); // groupAdmin = intended admin
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
        // 1. GroupMembership oluştur (APPROVED ve GROUP_ADMIN olarak)
        GroupMembership membership = new GroupMembership(intendedAdminId, newGroupId);
        membership.setStatus(GroupMembership.MembershipStatus.APPROVED);
        membership.setRole(GroupMembership.MembershipRole.GROUP_ADMIN); // KRİTİK: GROUP_ADMIN
        membership.setApprovedAt(LocalDateTime.now());
        membership.setApprovedBy(1); // System tarafından onaylandı

        // DOĞRUDAN KAYDET - processMembershipDecision çağırma
        membershipRepository.save(membership);

        // 2. User'ın groupId'sini güncelle
        transferUserToGroup(intendedAdminId, newGroupId);
    }

    /**
     * Kullanıcıyı yeni gruba transfer et
     */
    private void transferUserToGroup(Integer userId, Integer newGroupId) {
        // Kullanıcıyı bul
        User user = userRepository.findById(userId)
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