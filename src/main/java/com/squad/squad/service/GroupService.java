package com.squad.squad.service;

import com.squad.squad.dto.admin.AdminDecisionDTO;
import com.squad.squad.dto.group.GroupCreateRequestDTO;
import com.squad.squad.dto.group.GroupRequestResponseDTO;
import com.squad.squad.dto.group.GroupResponseDTO;

import java.util.List;

public interface GroupService {

    // Grup oluşturma talebi
    String createGroupRequest(GroupCreateRequestDTO request);

    // Bekleyen grup taleplerini getir (Super Admin için)
    List<GroupRequestResponseDTO> getPendingGroupRequests();

    // Grup talebini onayla/reddet (Super Admin)
    String processGroupRequest(Integer requestId, AdminDecisionDTO decision);

    // Onaylanmış grupları getir (üyelik başvurusu için)
    List<GroupResponseDTO> getApprovedGroups();

    // Kullanıcının grup taleplerini getir
    List<GroupRequestResponseDTO> getUserGroupRequests(Integer userId);
}