package com.squad.squad.controller;

import com.squad.squad.dto.squad.*;
import com.squad.squad.enums.GroupRole;
import com.squad.squad.service.SquadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/squads")
public class SquadController {

    private final SquadService squadService;

    public SquadController(SquadService squadService) {
        this.squadService = squadService;
    }

    // ==================== General (auth required, no group context) ====================

    @PostMapping("/request-create")
    public ResponseEntity<?> requestCreateSquad(@RequestBody CreateSquadRequestDTO dto) {
        try {
            squadService.requestCreateSquad(dto.getSquadName(), dto.getPlayerName(),
                    dto.getPlayerSurname(), dto.getPlayerPosition(), dto.getPlayerFoot());
            return ResponseEntity.ok("Squad creation request submitted successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/request-join")
    public ResponseEntity<?> requestJoinSquad(@RequestBody JoinSquadRequestDTO dto) {
        try {
            squadService.requestJoinSquad(dto.getInviteCode(), dto.getPlayerName(),
                    dto.getPlayerSurname(), dto.getPlayerPosition(), dto.getPlayerFoot());
            return ResponseEntity.ok("Join request submitted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/my-squads")
    public ResponseEntity<List<SquadSummaryDTO>> getMySquads() {
        return ResponseEntity.ok(squadService.getMySquads());
    }

    @GetMapping("/my-requests")
    public ResponseEntity<Map<String, Object>> getMyRequests() {
        return ResponseEntity.ok(squadService.getMyRequests());
    }

    @DeleteMapping("/cancel-squad/{requestId}")
    public ResponseEntity<?> cancelSquadRequest(@PathVariable Integer requestId) {
        try {
            squadService.cancelSquadRequest(requestId);
            return ResponseEntity.ok("Squad request cancelled");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/cancel-join/{requestId}")
    public ResponseEntity<?> cancelJoinRequest(@PathVariable Integer requestId) {
        try {
            squadService.cancelJoinRequest(requestId);
            return ResponseEntity.ok("Join request cancelled");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/my-player-id")
    public ResponseEntity<Integer> getMyPlayerId() {
        return ResponseEntity.ok(squadService.getMyPlayerId());
    }

    // ==================== Group Admin (group context required) ====================

    @GetMapping("/current")
    public ResponseEntity<SquadDetailDTO> getCurrentSquad() {
        return ResponseEntity.ok(squadService.getCurrentSquad());
    }

    @GetMapping("/admin/members")
    public ResponseEntity<List<MemberDTO>> getMembers() {
        return ResponseEntity.ok(squadService.getMembers());
    }

    @GetMapping("/admin/join-requests")
    public ResponseEntity<List<JoinRequestDTO>> getPendingJoinRequests() {
        return ResponseEntity.ok(squadService.getPendingJoinRequests());
    }

    @PutMapping("/admin/approve-join/{requestId}")
    public ResponseEntity<?> approveJoinRequest(@PathVariable Integer requestId) {
        squadService.approveJoinRequest(requestId);
        return ResponseEntity.ok("Join request approved");
    }

    @PutMapping("/admin/reject-join/{requestId}")
    public ResponseEntity<?> rejectJoinRequest(@PathVariable Integer requestId) {
        squadService.rejectJoinRequest(requestId);
        return ResponseEntity.ok("Join request rejected");
    }

    @DeleteMapping("/admin/remove-member/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Integer userId) {
        try {
            squadService.removeMember(userId);
            return ResponseEntity.ok("Member removed");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/admin/update-role/{userId}")
    public ResponseEntity<?> updateMemberRole(@PathVariable Integer userId, @RequestBody UpdateRoleRequestDTO dto) {
        try {
            GroupRole newRole = GroupRole.valueOf(dto.getRole().toUpperCase());
            squadService.updateMemberRole(userId, newRole);
            return ResponseEntity.ok("Role updated");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/admin/update")
    public ResponseEntity<?> updateSquadName(@RequestBody UpdateSquadNameDTO dto) {
        squadService.updateSquadName(dto.getName());
        return ResponseEntity.ok("Squad name updated");
    }

    @PostMapping("/admin/regenerate-invite")
    public ResponseEntity<Map<String, String>> regenerateInviteCode() {
        String newCode = squadService.regenerateInviteCode();
        return ResponseEntity.ok(Map.of("inviteCode", newCode));
    }

    // ==================== Super Admin ====================

    @GetMapping("/super/pending-requests")
    public ResponseEntity<List<SquadRequestDTO>> getPendingSquadRequests() {
        return ResponseEntity.ok(squadService.getPendingSquadRequests());
    }

    @PutMapping("/super/approve/{requestId}")
    public ResponseEntity<?> approveSquadRequest(@PathVariable Integer requestId) {
        squadService.approveSquadRequest(requestId);
        return ResponseEntity.ok("Squad request approved");
    }

    @PutMapping("/super/deactivate/{squadId}")
    public ResponseEntity<?> deactivateSquad(@PathVariable Integer squadId) {
        try {
            squadService.deactivateSquad(squadId);
            return ResponseEntity.ok("Squad deactivated");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/super/reject/{requestId}")
    public ResponseEntity<?> rejectSquadRequest(@PathVariable Integer requestId) {
        squadService.rejectSquadRequest(requestId);
        return ResponseEntity.ok("Squad request rejected");
    }

    @GetMapping("/super/all-squads")
    public ResponseEntity<List<SquadDetailDTO>> getAllSquads() {
        return ResponseEntity.ok(squadService.getAllSquads());
    }
}
