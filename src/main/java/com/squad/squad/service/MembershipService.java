package com.squad.squad.service;

import com.squad.squad.dto.admin.AdminDecisionDTO;
import com.squad.squad.dto.membership.MembershipRequestDTO;
import com.squad.squad.dto.membership.MembershipResponseDTO;

import java.util.List;

public interface MembershipService {

    // Grup üyelik başvurusu
    String requestMembership(MembershipRequestDTO request);

    // Bekleyen üyelik taleplerini getir (Group Admin için)
    List<MembershipResponseDTO> getPendingMemberships();

    // Üyelik talebini onayla/reddet (Group Admin)
    String processMembershipRequest(Integer membershipId, AdminDecisionDTO decision);

    // Kullanıcının üyelik durumunu getir
    List<MembershipResponseDTO> getUserMemberships(Integer userId);

    // Grup üyelerini getir (Group Admin için)
    List<MembershipResponseDTO> getGroupMembers(Integer groupId);
}