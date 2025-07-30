package com.squad.squad.controller;

import com.squad.squad.dto.admin.AdminDecisionDTO;
import com.squad.squad.dto.membership.MembershipResponseDTO;
import com.squad.squad.service.MembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-admin")
@PreAuthorize("isAuthenticated()")
public class GroupAdminController {

    private final MembershipService membershipService;

    @Autowired
    public GroupAdminController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    /**
     * Grup admininin yönettiği gruplardaki bekleyen üyelik taleplerini getir
     * Sadece grup adminleri veya super adminler erişebilir
     */
    @GetMapping("/pending-memberships")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @membershipService.isUserGroupAdmin(authentication.principal)")
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
     * Sadece o grubun adminleri veya super adminler erişebilir
     */
    @GetMapping("/group/{groupId}/members")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @membershipService.isUserAdminOfGroup(authentication.principal, #groupId)")
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
     * Sadece ilgili grubun adminleri veya super adminler erişebilir
     */
    @PutMapping("/{membershipId}/process")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @membershipService.canUserProcessMembership(authentication.principal, #membershipId)")
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