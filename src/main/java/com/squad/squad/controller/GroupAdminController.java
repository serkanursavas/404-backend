package com.squad.squad.controller;

import com.squad.squad.dto.admin.AdminDecisionDTO;
import com.squad.squad.dto.membership.MembershipResponseDTO;
import com.squad.squad.service.MembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-admin")
public class GroupAdminController {

    private final MembershipService membershipService;

    @Autowired
    public GroupAdminController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    /**
     * Grup admininin yönettiği gruplardaki bekleyen üyelik taleplerini getir
     */
    @GetMapping("/pending-memberships")
    public ResponseEntity<?> getPendingMemberships() {
        try {
            List<MembershipResponseDTO> memberships = membershipService.getPendingMemberships();
            return ResponseEntity.ok(memberships);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * Belirli bir grubun üyelerini getir
     */
    @GetMapping("/group/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable Integer groupId) {
        try {
            List<MembershipResponseDTO> members = membershipService.getGroupMembers(groupId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * Üyelik talebini onayla/reddet (Group Admin için)
     */
    @PutMapping("/{membershipId}/process")
    public ResponseEntity<?> processMembershipRequest(@PathVariable Integer membershipId,
            @RequestBody AdminDecisionDTO decision) {
        try {
            String result = membershipService.processMembershipRequest(membershipId, decision);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}