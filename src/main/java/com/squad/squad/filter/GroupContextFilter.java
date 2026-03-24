package com.squad.squad.filter;

import com.squad.squad.context.GroupContext;
import com.squad.squad.repository.GroupMembershipRepository;
import com.squad.squad.repository.SquadRepository;
import com.squad.squad.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class GroupContextFilter extends OncePerRequestFilter {

    private final GroupMembershipRepository groupMembershipRepository;
    private final SquadRepository squadRepository;

    private static final Set<String> EXEMPT_PATHS = Set.of(
            "/api/users/login",
            "/api/users/createUser",
            "/api/squads/request-create",
            "/api/squads/request-join",
            "/api/squads/my-squads",
            "/api/squads/my-requests",
            "/api/personas/savePersonas",
            "/api/notifications/device-token"
    );

    private static final Set<String> EXEMPT_PREFIXES = Set.of(
            "/api/squads/super/",
            "/api/users/updateProfile/",
            "/v3/api-docs",
            "/swagger-ui",
            "/error"
    );

    public GroupContextFilter(GroupMembershipRepository groupMembershipRepository, SquadRepository squadRepository) {
        this.groupMembershipRepository = groupMembershipRepository;
        this.squadRepository = squadRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String path = request.getRequestURI();

            // Check if path is exempt
            if (isExempt(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // If not authenticated, let security handle it
            if (authentication == null || !authentication.isAuthenticated()
                    || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
                filterChain.doFilter(request, response);
                return;
            }

            String groupIdHeader = request.getHeader("X-Group-Id");

            if (groupIdHeader == null || groupIdHeader.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "X-Group-Id header is required");
                return;
            }

            Integer groupId;
            try {
                groupId = Integer.parseInt(groupIdHeader);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid X-Group-Id header");
                return;
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer userId = userDetails.getId();

            // Verify user is member of this group
            if (!groupMembershipRepository.existsBySquadIdAndUserId(groupId, userId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not a member of this group");
                return;
            }

            if (!squadRepository.existsByIdAndActiveTrue(groupId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Squad is not active");
                return;
            }

            GroupContext.setCurrentGroupId(groupId);
            filterChain.doFilter(request, response);

        } finally {
            GroupContext.clear();
        }
    }

    private boolean isExempt(String path) {
        if (EXEMPT_PATHS.contains(path)) {
            return true;
        }
        for (String prefix : EXEMPT_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
