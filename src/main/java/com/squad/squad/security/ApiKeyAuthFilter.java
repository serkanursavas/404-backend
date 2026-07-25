package com.squad.squad.security;

import com.squad.squad.context.GroupContext;
import com.squad.squad.entity.User;
import com.squad.squad.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;

/**
 * Apple Shortcuts entegrasyonu için statik API-key doğrulaması.
 * Sadece /api/shortcut/** path'inde çalışır; diğer tüm isteklerde no-op'tur.
 * Geçerli key ile gelen istekte, önceden yapılandırılmış (env-tabanlı) admin
 * kullanıcıyı authenticate eder ve GroupContext'i manuel set eder — böylece
 * JwtAuthenticationFilter ve GroupContextFilter'ın normal akışı, JWT/X-Group-Id
 * olmadan da bu path için sorunsuz işler.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String SHORTCUT_PATH_PREFIX = "/api/shortcut";

    private final UserRepository userRepository;

    @Value("${shortcut.api-key:}")
    private String configuredApiKey;

    @Value("${shortcut.admin-user-id:}")
    private String configuredAdminUserId;

    @Value("${shortcut.squad-id:}")
    private String configuredSquadId;

    public ApiKeyAuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getRequestURI().startsWith(SHORTCUT_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (configuredApiKey == null || configuredApiKey.isBlank()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Shortcut entegrasyonu yapılandırılmamış");
            return;
        }

        String providedKey = request.getHeader("X-Api-Key");
        if (providedKey == null || !constantTimeEquals(providedKey, configuredApiKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Geçersiz API key");
            return;
        }

        Integer adminUserId = parseOrNull(configuredAdminUserId);
        Integer squadId = parseOrNull(configuredSquadId);
        if (adminUserId == null || squadId == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Shortcut kullanıcı/squad yapılandırması eksik");
            return;
        }

        User user = userRepository.findById(adminUserId).orElse(null);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Shortcut kullanıcısı bulunamadı");
            return;
        }

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.isSuperAdmin() ? "ROLE_SUPER_ADMIN" : "ROLE_USER"));
        CustomUserDetails userDetails = new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), authorities);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        GroupContext.setCurrentGroupId(squadId);

        filterChain.doFilter(request, response);
    }

    private boolean constantTimeEquals(String provided, String expected) {
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }

    private Integer parseOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }
}
