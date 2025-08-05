package com.squad.squad.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class JwtGroupContextService {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * JWT token'dan approved group ID'sini al
     */
    public Integer getCurrentApprovedGroupId() {
        String token = getCurrentJwtToken();
        if (token == null) {
            return null;
        }
        return jwtUtils.extractApprovedGroupId(token);
    }

    /**
     * JWT token'dan user ID'sini al
     */
    public Integer getCurrentUserId() {
        String token = getCurrentJwtToken();
        if (token == null) {
            return null;
        }
        return jwtUtils.extractId(token);
    }

    /**
     * JWT token'dan group ID'sini al
     */
    public Integer getCurrentGroupId() {
        String token = getCurrentJwtToken();
        if (token == null) {
            return null;
        }
        return jwtUtils.extractGroupId(token);
    }

    /**
     * JWT token'dan role'ü al
     */
    public String getCurrentUserRole() {
        String token = getCurrentJwtToken();
        if (token == null) {
            return null;
        }
        return jwtUtils.extractRole(token);
    }

    /**
     * Mevcut JWT token'ı al
     */
    private String getCurrentJwtToken() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String token = (String) request.getAttribute("JWT_TOKEN");
                if (token != null) {
                    return token;
                }
            }
        } catch (Exception e) {
            // Log error if needed
        }

        // Fallback: Authorization header'dan al
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7);
                }
            }
        } catch (Exception e) {
            // Log error if needed
        }

        return null;
    }

    /**
     * Kullanıcının admin olup olmadığını kontrol et
     */
    public boolean isAdmin() {
        String role = getCurrentUserRole();
        return "ROLE_ADMIN".equals(role);
    }

    /**
     * Kullanıcının onaylanmış grubu olup olmadığını kontrol et
     */
    public boolean hasApprovedGroup() {
        Integer approvedGroupId = getCurrentApprovedGroupId();
        return approvedGroupId != null && approvedGroupId > 0;
    }
}