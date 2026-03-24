package com.squad.squad.service;

import com.squad.squad.context.GroupContext;
import com.squad.squad.entity.GroupMembership;
import com.squad.squad.entity.User;
import com.squad.squad.enums.GroupRole;
import com.squad.squad.repository.GroupMembershipRepository;
import com.squad.squad.repository.UserRepository;
import com.squad.squad.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class GroupAuthorizationService {

    private final GroupMembershipRepository groupMembershipRepository;
    private final UserRepository userRepository;

    public GroupAuthorizationService(GroupMembershipRepository groupMembershipRepository, UserRepository userRepository) {
        this.groupMembershipRepository = groupMembershipRepository;
        this.userRepository = userRepository;
    }

    public Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("Current user not found");
    }

    public GroupMembership getCurrentMembership() {
        Integer groupId = GroupContext.getCurrentGroupId();
        Integer userId = getCurrentUserId();
        return groupMembershipRepository.findBySquadIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalStateException("User is not a member of this group"));
    }

    public Integer getCurrentPlayerId() {
        return getCurrentMembership().getPlayer().getId();
    }

    public void requireAdmin() {
        if (isSuperAdmin()) return;
        GroupMembership membership = getCurrentMembership();
        if (membership.getRole() != GroupRole.ADMIN) {
            throw new SecurityException("Admin access required");
        }
    }

    public void requireSuperAdmin() {
        Integer userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (!user.isSuperAdmin()) {
            throw new SecurityException("Super admin access required");
        }
    }

    public boolean isAdmin() {
        try {
            GroupMembership membership = getCurrentMembership();
            return membership.getRole() == GroupRole.ADMIN;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSuperAdmin() {
        try {
            Integer userId = getCurrentUserId();
            User user = userRepository.findById(userId).orElse(null);
            return user != null && user.isSuperAdmin();
        } catch (Exception e) {
            return false;
        }
    }
}
