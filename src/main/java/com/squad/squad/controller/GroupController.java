package com.squad.squad.controller;

import com.squad.squad.dto.admin.AdminDecisionDTO;
import com.squad.squad.dto.group.GroupCreateRequestDTO;
import com.squad.squad.dto.group.GroupRequestResponseDTO;
import com.squad.squad.dto.group.GroupResponseDTO;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.service.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/public/approved")
    public ResponseEntity<List<GroupResponseDTO>> getApprovedGroupsForSignup() {
        List<GroupResponseDTO> groups = groupService.getApprovedGroups();
        return ResponseEntity.ok(groups);
    }

    /**
     * Grup oluşturma talebi gönder
     */
    @PostMapping("/request")
    public ResponseEntity<?> createGroupRequest(@RequestBody GroupCreateRequestDTO request) {
        try {
            String result = groupService.createGroupRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Grup talebi oluşturulurken bir hata oluştu.");
        }
    }

    /**
     * Bekleyen grup taleplerini getir (Super Admin için)
     */
    @GetMapping("/admin/pending")
    public ResponseEntity<?> getPendingGroupRequests() {
        try {
            List<GroupRequestResponseDTO> requests = groupService.getPendingGroupRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * Grup talebini onayla/reddet (Super Admin için)
     */
    @PutMapping("/admin/{requestId}/process")
    public ResponseEntity<?> processGroupRequest(@PathVariable Integer requestId,
            @RequestBody AdminDecisionDTO decision) {
        try {
            String result = groupService.processGroupRequest(requestId, decision);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * Onaylanmış grupları getir (üyelik başvurusu için)
     */
    @GetMapping("/approved")
    public ResponseEntity<List<GroupResponseDTO>> getApprovedGroups() {
        List<GroupResponseDTO> groups = groupService.getApprovedGroups();
        return ResponseEntity.ok(groups);
    }

    /**
     * Kullanıcının grup taleplerini getir
     */
    @GetMapping("/my-requests")
    public ResponseEntity<List<GroupRequestResponseDTO>> getUserGroupRequests() {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        List<GroupRequestResponseDTO> requests = groupService.getUserGroupRequests(currentUser.getId());
        return ResponseEntity.ok(requests);
    }
}