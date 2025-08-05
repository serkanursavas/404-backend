package com.squad.squad.filter;

import com.squad.squad.context.GroupContext;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.security.JwtUtils;
import com.squad.squad.repository.GroupMembershipRepository;
import com.squad.squad.entity.GroupMembership;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsService customUserDetailsService;
    private final GroupMembershipRepository groupMembershipRepository;

    @Autowired
    public JwtAuthenticationFilter(@Lazy JwtUtils jwtUtils,
            @Lazy UserDetailsService customUserDetailsService,
            @Lazy GroupMembershipRepository groupMembershipRepository) {
        this.jwtUtils = jwtUtils;
        this.customUserDetailsService = customUserDetailsService;
        this.groupMembershipRepository = groupMembershipRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        // JWT token'ı extract et
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            try {
                username = jwtUtils.extractUsername(token);
            } catch (Exception e) {
                logger.debug("Failed to extract username from token: {}", e.getMessage());
            }
        }

        // JWT validation ve authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                if (jwtUtils.validateToken(token, userDetails.getUsername())) {
                    // Authentication token oluştur
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Group context'i set et
                    setupGroupContext(userDetails, username, token, request);

                    // Current user ID'yi set et
                    if (userDetails instanceof CustomUserDetails) {
                        GroupContext.setCurrentUserId(((CustomUserDetails) userDetails).getId());
                    }
                }
            } catch (Exception e) {
                logger.error("Authentication failed for user: {}", username, e);
                SecurityContextHolder.clearContext();
                GroupContext.clear();
            }
        }

        // Authenticated user'ların signup/login endpoint'lerine erişimini engelle
        boolean isAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated();

        String path = request.getRequestURI();
        if (isAuthenticated && (path.equals("/api/users/createUser") || path.equals("/api/users/login"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authenticated users cannot access this endpoint");
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Request sonunda context'i temizle
            clearContexts(path);
        }
    }

    /**
     * JWT token'a group bilgilerini ekle
     */
    private void setupGroupContext(UserDetails userDetails, String username, String token, HttpServletRequest request) {
        if (userDetails instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

            try {
                Integer approvedGroupId = null;

                // Super Admin kontrolü
                if ("ROLE_ADMIN".equals(customUserDetails.getRole())) {
                    // Super admin için kendi grubunun ID'sini set et
                    approvedGroupId = customUserDetails.getGroupId();
                    logger.debug("Super Admin approvedGroupId set for user: {} with groupId: {}", username,
                            approvedGroupId);
                } else {
                    // Normal user için approved group ID - sadece onaylanmış üyelikleri dikkate al
                    List<GroupMembership> approvedMemberships = groupMembershipRepository.findByUserIdAndStatus(
                            customUserDetails.getId(), GroupMembership.MembershipStatus.APPROVED);

                    if (!approvedMemberships.isEmpty()) {
                        // İlk onaylanmış üyeliği al
                        approvedGroupId = approvedMemberships.get(0).getGroupId();
                        logger.debug("Approved group ID set for user: {} with groupId: {}", username, approvedGroupId);
                    } else {
                        approvedGroupId = null; // Onaylanmamış üyelik
                        logger.debug("No approved membership found for user: {}", username);
                    }
                }

                // JWT token'ı approvedGroupId ile güncelle
                String updatedToken = jwtUtils.generateToken(
                        customUserDetails.getId(),
                        customUserDetails.getUsername(),
                        customUserDetails.getRole(),
                        customUserDetails.getGroupId(),
                        approvedGroupId);

                // Token'ı request attribute'a set et (JwtGroupContextService için)
                request.setAttribute("JWT_TOKEN", updatedToken);

                logger.debug("JWT token updated with approvedGroupId for user: {}", username);

            } catch (Exception e) {
                logger.error("Failed to setup group context for user: {}", username, e);
                SecurityContextHolder.clearContext();
                throw new RuntimeException("Authentication context setup failed", e);
            }
        } else {
            logger.warn("UserDetails is not instance of CustomUserDetails for user: {}", username);
        }
    }

    /**
     * Request sonunda context'leri temizle
     */
    private void clearContexts(String path) {
        try {
            if (GroupContext.hasGroupId()) {
                GroupContext.clear();
                logger.debug("Group context cleared for request: {}", path);
            }
            // Current user ID'yi de temizle
            GroupContext.clearCurrentUserId();
        } catch (Exception e) {
            logger.warn("Failed to clear group context for request: {}, error: {}", path, e.getMessage());
        }
    }

    /**
     * Public endpoint'leri kontrol et (group context gerektirmeyen)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Public endpoint'ler
        return path.equals("/api/users/createUser") ||
                path.equals("/api/users/login") ||
                path.startsWith("/api/groups/public/") ||
                path.equals("/api/groups/approved") ||
                path.startsWith("/error") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui");
    }
}