package com.squad.squad.filter;

import com.squad.squad.context.TenantContext;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.security.JwtUtils;
import com.squad.squad.service.TenantContextService;
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
    private final TenantContextService tenantContextService;

    @Autowired
    public JwtAuthenticationFilter(@Lazy JwtUtils jwtUtils,
                                   @Lazy UserDetailsService customUserDetailsService,
                                   @Lazy TenantContextService tenantContextService) {
        this.jwtUtils = jwtUtils;
        this.customUserDetailsService = customUserDetailsService;
        this.tenantContextService = tenantContextService;
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

                    // Tenant context'i set et
                    setupTenantContext(userDetails, username);
                }
            } catch (Exception e) {
                logger.error("Authentication failed for user: {}", username, e);
                SecurityContextHolder.clearContext();
                TenantContext.clear();
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
     * Tenant context'i setup et
     */
    private void setupTenantContext(UserDetails userDetails, String username) {
        if (userDetails instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

            try {
                // Super Admin kontrolü
                if ("ROLE_ADMIN".equals(customUserDetails.getRole())) {
                    tenantContextService.setSuperAdminContext();
                    logger.debug("Super Admin context set for user: {}", username);
                } else {
                    // Normal user için tenant context
                    Integer groupId = customUserDetails.getGroupId();
                    if (groupId != null && groupId > 0) {
                        tenantContextService.setTenantContext(groupId);
                        logger.debug("Tenant context set for user: {} with groupId: {}", username, groupId);
                    } else {
                        // Grup ID yoksa (pending user) sadece user context set et
                        tenantContextService.setUserContext();
                        logger.debug("User context set for pending user: {}", username);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to set tenant context for user: {}", username, e);
                // Tenant context set edilemezse güvenlik için authentication'ı temizle
                SecurityContextHolder.clearContext();
                TenantContext.clear();
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
            if (TenantContext.hasTenant()) {
                tenantContextService.clearTenantContext();
                logger.debug("Tenant context cleared for request: {}", path);
            }
        } catch (Exception e) {
            logger.warn("Failed to clear tenant context for request: {}, error: {}", path, e.getMessage());
        }
    }

    /**
     * Public endpoint'leri kontrol et (tenant context gerektirmeyen)
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