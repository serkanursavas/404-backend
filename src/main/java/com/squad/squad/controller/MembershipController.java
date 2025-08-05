package com.squad.squad.controller;

import com.squad.squad.dto.admin.AdminDecisionDTO;
import com.squad.squad.dto.membership.MembershipRequestDTO;
import com.squad.squad.dto.membership.MembershipResponseDTO;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.service.MembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memberships")
@PreAuthorize("isAuthenticated()")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    /**
     * Grup üyelik başvurusu gönder
     * Sadece authenticated kullanıcılar grup başvurusu yapabilir
     */
    @PostMapping("/request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> requestMembership(@RequestBody MembershipRequestDTO request) {
        try {
            String result = membershipService.requestMembership(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Üyelik başvurusu gönderilirken bir hata oluştu.");
        }
    }

    /**
     * Bekleyen üyelik taleplerini getir (Group Admin için)
     * Sadece grup adminleri veya super adminler erişebilir
     */
    @GetMapping("/pending")
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

    /**
     * Kullanıcının üyeliklerini getir
     * Sadece authenticated kullanıcılar kendi üyeliklerini görebilir
     */
    @GetMapping("/my-memberships")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MembershipResponseDTO>> getUserMemberships() {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        List<MembershipResponseDTO> memberships = membershipService.getUserMemberships(currentUser.getId());
        return ResponseEntity.ok(memberships);
    }

    /**
     * Grup üyelerini getir (Group Admin için)
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
}