package com.squad.squad.filter;

import com.squad.squad.config.RLSHelper;
import com.squad.squad.context.TenantContext;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.security.JwtUtils;
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
    private final RLSHelper rlsHelper;

    @Autowired
    public JwtAuthenticationFilter(@Lazy JwtUtils jwtUtils, @Lazy UserDetailsService customUserDetailsService, RLSHelper rlsHelper) {
        this.jwtUtils = jwtUtils;
        this.customUserDetailsService = customUserDetailsService;
        this.rlsHelper = rlsHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            username = jwtUtils.extractUsername(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            if (jwtUtils.validateToken(token, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // RLS için tenant context'i set et
                if (userDetails instanceof CustomUserDetails) {
                    CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
                    Integer groupId = customUserDetails.getGroupId();

                    if (groupId != null) {
                        // Java context'i set et
                        TenantContext.setTenantId(groupId);

                        // PostgreSQL session'ında RLS context'i set et
                        try {
                            rlsHelper.setTenantContext();
                            logger.debug("RLS context set for user: {} with groupId: {}", username, groupId);
                        } catch (Exception e) {
                            logger.error("Failed to set RLS context for user: {}", username, e);
                            // RLS context set edilemezse güvenlik için authentication'ı temizle
                            SecurityContextHolder.clearContext();
                            TenantContext.clear();
                            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication failed");
                            return;
                        }
                    } else {
                        logger.warn("User {} has no groupId, cannot set tenant context", username);
                    }
                }
            }
        }

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
            if (TenantContext.hasTenant()) {
                TenantContext.clear();
                logger.debug("Tenant context cleared for request: {}", path);
            }
        }
    }
}