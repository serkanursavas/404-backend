package com.squad.squad.controller;

import com.squad.squad.dto.squad.*;
import com.squad.squad.entity.*;
import com.squad.squad.enums.GroupRole;
import com.squad.squad.enums.RequestStatus;
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
        List<GroupMembership> memberships = squadService.getMySquads();
        List<SquadSummaryDTO> squads = memberships.stream()
                .map(m -> new SquadSummaryDTO(m.getSquad().getId(), m.getSquad().getName(), m.getRole().name()))
                .toList();
        return ResponseEntity.ok(squads);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyRequests() {
        Object[] data = squadService.getMyRequests().toArray();

        // Build response manually
        Object[] squadRequestsPair = (Object[]) data[0];
        Object[] joinRequestsPair = (Object[]) data[1];

        @SuppressWarnings("unchecked")
        List<SquadRequest> squadRequests = (List<SquadRequest>) squadRequestsPair[1];
        @SuppressWarnings("unchecked")
        List<JoinRequest> joinRequests = (List<JoinRequest>) joinRequestsPair[1];

        List<SquadRequestDTO> squadRequestDTOs = squadRequests.stream()
                .map(r -> {
                    SquadRequestDTO dto = new SquadRequestDTO();
                    dto.setId(r.getId());
                    dto.setSquadName(r.getName());
                    dto.setStatus(r.getStatus().name());
                    dto.setCreatedAt(r.getCreatedAt());
                    return dto;
                }).toList();

        List<JoinRequestDTO> joinRequestDTOs = joinRequests.stream()
                .map(r -> {
                    JoinRequestDTO dto = new JoinRequestDTO();
                    dto.setId(r.getId());
                    dto.setSquadName(r.getSquad().getName());
                    dto.setAdminUsername(squadService.getAdminUsernameForSquad(r.getSquad().getId()));
                    dto.setPlayerName(r.getPlayerName());
                    dto.setPlayerSurname(r.getPlayerSurname());
                    dto.setStatus(r.getStatus().name());
                    dto.setCreatedAt(r.getCreatedAt());
                    return dto;
                }).toList();

        int pendingCount = (int) squadRequests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count()
                + (int) joinRequests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count();

        return ResponseEntity.ok(Map.of(
                "squadRequests", squadRequestDTOs,
                "joinRequests", joinRequestDTOs,
                "pendingCount", pendingCount
        ));
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

    // ==================== Group Admin (group context required) ====================

    @GetMapping("/current")
    public ResponseEntity<SquadDetailDTO> getCurrentSquad() {
        Squad squad = squadService.getCurrentSquad();
        SquadDetailDTO dto = new SquadDetailDTO();
        dto.setId(squad.getId());
        dto.setName(squad.getName());
        dto.setInviteCode(squad.getInviteCode());
        dto.setCreatedAt(squad.getCreatedAt());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/admin/members")
    public ResponseEntity<List<MemberDTO>> getMembers() {
        List<GroupMembership> memberships = squadService.getMembers();
        List<MemberDTO> members = memberships.stream()
                .map(m -> {
                    MemberDTO dto = new MemberDTO();
                    dto.setUserId(m.getUser().getId());
                    dto.setUsername(m.getUser().getUsername());
                    dto.setPlayerId(m.getPlayer().getId());
                    dto.setPlayerName(m.getPlayer().getName());
                    dto.setPlayerSurname(m.getPlayer().getSurname());
                    dto.setRole(m.getRole().name());
                    dto.setJoinedAt(m.getJoinedAt());
                    return dto;
                }).toList();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/admin/join-requests")
    public ResponseEntity<List<JoinRequestDTO>> getPendingJoinRequests() {
        List<JoinRequest> requests = squadService.getPendingJoinRequests();
        List<JoinRequestDTO> dtos = requests.stream()
                .map(r -> {
                    JoinRequestDTO dto = new JoinRequestDTO();
                    dto.setId(r.getId());
                    dto.setUsername(r.getUser().getUsername());
                    dto.setPlayerName(r.getPlayerName());
                    dto.setPlayerSurname(r.getPlayerSurname());
                    dto.setPlayerPosition(r.getPlayerPosition());
                    dto.setPlayerFoot(r.getPlayerFoot());
                    dto.setStatus(r.getStatus().name());
                    dto.setCreatedAt(r.getCreatedAt());
                    return dto;
                }).toList();
        return ResponseEntity.ok(dtos);
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
        List<SquadRequest> requests = squadService.getPendingSquadRequests();
        List<SquadRequestDTO> dtos = requests.stream()
                .map(r -> {
                    SquadRequestDTO dto = new SquadRequestDTO();
                    dto.setId(r.getId());
                    dto.setSquadName(r.getName());
                    dto.setRequestedByUsername(r.getRequestedByUser().getUsername());
                    dto.setPlayerName(r.getPlayerName());
                    dto.setPlayerSurname(r.getPlayerSurname());
                    dto.setStatus(r.getStatus().name());
                    dto.setCreatedAt(r.getCreatedAt());
                    return dto;
                }).toList();
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/super/approve/{requestId}")
    public ResponseEntity<?> approveSquadRequest(@PathVariable Integer requestId) {
        squadService.approveSquadRequest(requestId);
        return ResponseEntity.ok("Squad request approved");
    }

    @PutMapping("/super/reject/{requestId}")
    public ResponseEntity<?> rejectSquadRequest(@PathVariable Integer requestId) {
        squadService.rejectSquadRequest(requestId);
        return ResponseEntity.ok("Squad request rejected");
    }

    @GetMapping("/super/all-squads")
    public ResponseEntity<List<SquadDetailDTO>> getAllSquads() {
        List<Squad> squads = squadService.getAllSquads();
        List<SquadDetailDTO> dtos = squads.stream()
                .map(s -> {
                    SquadDetailDTO dto = new SquadDetailDTO();
                    dto.setId(s.getId());
                    dto.setName(s.getName());
                    dto.setInviteCode(s.getInviteCode());
                    dto.setCreatedAt(s.getCreatedAt());
                    return dto;
                }).toList();
        return ResponseEntity.ok(dtos);
    }
}
