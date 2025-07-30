package com.squad.squad.service;

import com.squad.squad.context.TenantContext;
import com.squad.squad.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantContextServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TenantContextService tenantContextService;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;
    private MockedStatic<TenantContext> tenantContextMock;

    @BeforeEach
    void setUp() {
        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        tenantContextMock = mockStatic(TenantContext.class);
        
        // SecurityContextHolder mock setup
        securityContextHolderMock.when(SecurityContextHolder::getContext)
                .thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMock.close();
        tenantContextMock.close();
    }

    @Test
    void testSetTenantContext_WithValidUser_NormalGroup() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 5, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        tenantContextService.setTenantContext(5);

        // Then
        verify(jdbcTemplate).execute("SET app.current_user_id = '1'");
        verify(jdbcTemplate).execute("SET app.current_tenant_id = '5'");
        verify(jdbcTemplate).execute("SET app.group_id = '5'");
        tenantContextMock.verify(() -> TenantContext.setTenantId(5));
    }

    @Test
    void testSetTenantContext_WithValidUser_PendingGroup() {
        // Given
        CustomUserDetails userDetails = createMockUser(2, 0, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        tenantContextService.setTenantContext(0);

        // Then
        verify(jdbcTemplate).execute("SET app.current_user_id = '2'");
        verify(jdbcTemplate).execute("SET app.current_tenant_id = '0'");
        verify(jdbcTemplate).execute("SET app.group_id = '0'");
        tenantContextMock.verify(() -> TenantContext.setTenantId(0));
    }

    @Test
    void testSetTenantContext_WithValidUser_NullGroupId() {
        // Given
        CustomUserDetails userDetails = createMockUser(3, 1, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        tenantContextService.setTenantContext(null);

        // Then
        verify(jdbcTemplate).execute("SET app.current_user_id = '3'");
        verify(jdbcTemplate).execute("SET app.current_tenant_id = ''");
        verify(jdbcTemplate).execute("SET app.group_id = ''");
        tenantContextMock.verify(() -> TenantContext.setTenantId(null));
    }

    @Test
    void testSetTenantContext_WithNoUser() {
        // Given
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // When
        tenantContextService.setTenantContext(5);

        // Then
        verify(jdbcTemplate, never()).execute(anyString());
        tenantContextMock.verify(() -> TenantContext.setTenantId(5), never());
    }

    @Test
    void testSetTenantContext_WithNullAuthentication() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        tenantContextService.setTenantContext(5);

        // Then
        verify(jdbcTemplate, never()).execute(anyString());
        tenantContextMock.verify(() -> TenantContext.setTenantId(5), never());
    }

    @Test
    void testSetUserContext_WithExistingTenantContext() {
        // Given
        CustomUserDetails userDetails = createMockUser(4, 3, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(3);

        // When
        tenantContextService.setUserContext();

        // Then
        verify(jdbcTemplate).execute("SET app.current_user_id = '4'");
        // setTenantContext çağrılmamalı çünkü tenant context zaten var
        verify(jdbcTemplate, never()).execute("SET app.current_tenant_id = '3'");
    }

    @Test
    void testSetUserContext_WithoutTenantContext() {
        // Given
        CustomUserDetails userDetails = createMockUser(5, 2, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(null);

        // When
        tenantContextService.setUserContext();

        // Then
        verify(jdbcTemplate).execute("SET app.current_user_id = '5'");
        // User'ın grup ID'si ile tenant context set edilmeli
        verify(jdbcTemplate).execute("SET app.current_tenant_id = '2'");
        verify(jdbcTemplate).execute("SET app.group_id = '2'");
    }

    @Test
    void testClearTenantContext() {
        // When
        tenantContextService.clearTenantContext();

        // Then
        verify(jdbcTemplate).execute("SET app.current_tenant_id = ''");
        verify(jdbcTemplate).execute("SET app.group_id = ''");
        verify(jdbcTemplate).execute("SET app.current_user_id = ''");
        tenantContextMock.verify(TenantContext::clear);
    }

    @Test
    void testSetSuperAdminContext_WithSuperAdmin() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 1, "ROLE_ADMIN");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        tenantContextService.setSuperAdminContext();

        // Then
        verify(jdbcTemplate).execute("SET app.current_user_id = '1'");
        verify(jdbcTemplate).execute("SET app.current_tenant_id = 'SUPER_ADMIN'");
        verify(jdbcTemplate).execute("SET app.group_id = 'SUPER_ADMIN'");
        tenantContextMock.verify(TenantContext::clear);
    }

    @Test
    void testSetSuperAdminContext_WithNormalUser() {
        // Given
        CustomUserDetails userDetails = createMockUser(2, 1, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        tenantContextService.setSuperAdminContext();

        // Then
        // Super admin olmadığı için hiçbir SQL execute edilmemeli
        verify(jdbcTemplate, never()).execute(anyString());
        tenantContextMock.verify(TenantContext::clear, never());
    }

    @Test
    void testGetCurrentTenantId() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(5);

        // When
        Integer result = tenantContextService.getCurrentTenantId();

        // Then
        assertEquals(5, result);
        tenantContextMock.verify(TenantContext::getTenantId);
    }

    @Test
    void testIsSuperAdmin_WithSuperAdmin() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 1, "ROLE_ADMIN");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        boolean result = tenantContextService.isSuperAdmin();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsSuperAdmin_WithNormalUser() {
        // Given
        CustomUserDetails userDetails = createMockUser(2, 1, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        boolean result = tenantContextService.isSuperAdmin();

        // Then
        assertFalse(result);
    }

    @Test
    void testIsSuperAdmin_WithNoUser() {
        // Given
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // When
        boolean result = tenantContextService.isSuperAdmin();

        // Then
        assertFalse(result);
    }

    @Test
    void testInitializeContext_WithSuperAdmin() {
        // Given
        CustomUserDetails userDetails = createMockUser(1, 1, "ROLE_ADMIN");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        tenantContextService.initializeContext();

        // Then
        verify(jdbcTemplate).execute("SET app.current_user_id = '1'");
        verify(jdbcTemplate).execute("SET app.current_tenant_id = 'SUPER_ADMIN'");
        verify(jdbcTemplate).execute("SET app.group_id = 'SUPER_ADMIN'");
        tenantContextMock.verify(TenantContext::clear);
    }

    @Test
    void testInitializeContext_WithNormalUser() {
        // Given
        CustomUserDetails userDetails = createMockUser(2, 3, "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        tenantContextService.initializeContext();

        // Then
        verify(jdbcTemplate).execute("SET app.current_user_id = '2'");
        verify(jdbcTemplate).execute("SET app.current_tenant_id = '3'");
        verify(jdbcTemplate).execute("SET app.group_id = '3'");
        tenantContextMock.verify(() -> TenantContext.setTenantId(3));
    }

    @Test
    void testInitializeContext_WithNoUser() {
        // Given
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // When
        tenantContextService.initializeContext();

        // Then
        verify(jdbcTemplate, never()).execute(anyString());
    }

    private CustomUserDetails createMockUser(Integer id, Integer groupId, String role) {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(id);
        when(userDetails.getGroupId()).thenReturn(groupId);
        when(userDetails.getRole()).thenReturn(role);
        when(userDetails.getUsername()).thenReturn("testuser" + id);
        return userDetails;
    }
}