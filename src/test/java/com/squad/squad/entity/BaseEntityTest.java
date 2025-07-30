package com.squad.squad.entity;

import com.squad.squad.context.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseEntityTest {

    private MockedStatic<TenantContext> tenantContextMock;
    private TestEntity testEntity;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        testEntity = new TestEntity();
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Test
    void testGroupIdGetterSetter() {
        // Given
        Integer expectedGroupId = 5;

        // When
        testEntity.setGroupId(expectedGroupId);

        // Then
        assertEquals(expectedGroupId, testEntity.getGroupId());
    }

    @Test
    void testPrePersist_WithExistingGroupId() {
        // Given
        testEntity.setGroupId(10);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(5);

        // When
        testEntity.prePersist();

        // Then
        assertEquals(10, testEntity.getGroupId()); // GroupId değişmemeli
        tenantContextMock.verify(TenantContext::getTenantId, never());
    }

    @Test
    void testPrePersist_WithNullGroupId_AuthenticatedUser() {
        // Given
        testEntity.setGroupId(null);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(5);

        // When
        testEntity.prePersist();

        // Then
        assertEquals(5, testEntity.getGroupId());
        tenantContextMock.verify(TenantContext::getTenantId);
    }

    @Test
    void testPrePersist_WithNullGroupId_NoAuthentication() {
        // Given
        testEntity.setGroupId(null);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(null);

        // When
        testEntity.prePersist();

        // Then
        assertEquals(0, testEntity.getGroupId()); // Pending group
        tenantContextMock.verify(TenantContext::getTenantId);
    }

    @Test
    void testPrePersist_WithNullGroupId_ZeroTenantId() {
        // Given
        testEntity.setGroupId(null);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(0);

        // When
        testEntity.prePersist();

        // Then
        assertEquals(0, testEntity.getGroupId()); // Tenant 0 da pending group olarak kullanılıyor
        tenantContextMock.verify(TenantContext::getTenantId);
    }

    @Test
    void testPrePersist_WithNullGroupId_NegativeTenantId() {
        // Given
        testEntity.setGroupId(null);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(-1);

        // When
        testEntity.prePersist();

        // Then
        assertEquals(0, testEntity.getGroupId()); // Negatif tenant ID'ler için pending group
        tenantContextMock.verify(TenantContext::getTenantId);
    }

    @Test
    void testPreUpdate_SameTenant() {
        // Given
        testEntity.setGroupId(5);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(5);

        // When & Then
        assertDoesNotThrow(() -> testEntity.preUpdate());
        tenantContextMock.verify(TenantContext::getTenantId);
    }

    @Test
    void testPreUpdate_DifferentTenant() {
        // Given
        testEntity.setGroupId(5);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(10);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> testEntity.preUpdate());

        assertEquals("Cannot modify entity belonging to different tenant. Entity tenant: 5, Current tenant: 10",
                exception.getMessage());
        tenantContextMock.verify(TenantContext::getTenantId);
    }

    @Test
    void testPreUpdate_NullCurrentTenant() {
        // Given
        testEntity.setGroupId(5);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(null);

        // When & Then
        assertDoesNotThrow(() -> testEntity.preUpdate());
        tenantContextMock.verify(TenantContext::getTenantId);
    }

    @Test
    void testPreUpdate_NullEntityGroupId() {
        // Given
        testEntity.setGroupId(null);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(5);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> testEntity.preUpdate());

        assertEquals("Cannot modify entity belonging to different tenant. Entity tenant: null, Current tenant: 5",
                exception.getMessage());
        tenantContextMock.verify(TenantContext::getTenantId);
    }

    @Test
    void testPreUpdate_BothNull() {
        // Given
        testEntity.setGroupId(null);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(null);

        // When & Then
        assertDoesNotThrow(() -> testEntity.preUpdate());
        tenantContextMock.verify(TenantContext::getTenantId);
    }

    @Test
    void testPrePersistAndPreUpdateIntegration() {
        // Given - Yeni entity
        testEntity.setGroupId(null);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(7);

        // When - PrePersist çağrılır
        testEntity.prePersist();

        // Then - GroupId set edilmeli
        assertEquals(7, testEntity.getGroupId());

        // When - Aynı tenant context ile PreUpdate çağrılır
        assertDoesNotThrow(() -> testEntity.preUpdate());

        // When - Farklı tenant context ile PreUpdate çağrılır
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(12);

        // Then - Exception fırlatmalı
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> testEntity.preUpdate());

        assertEquals("Cannot modify entity belonging to different tenant. Entity tenant: 7, Current tenant: 12",
                exception.getMessage());
    }

    @Test
    void testTenantContextBoundary_ZeroValues() {
        // Test boundary condition where tenant ID is exactly 0
        testEntity.setGroupId(null);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(0);

        testEntity.prePersist();

        assertEquals(0, testEntity.getGroupId());
    }

    @Test
    void testTenantContextBoundary_PositiveValues() {
        // Test boundary condition where tenant ID is exactly 1
        testEntity.setGroupId(null);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(1);

        testEntity.prePersist();

        assertEquals(1, testEntity.getGroupId());
    }

    @Test
    void testAbstractEntityInheritance() {
        // Verify that TestEntity correctly extends BaseEntity
        assertInstanceOf(BaseEntity.class, testEntity);
        
        // Verify that BaseEntity methods are accessible
        testEntity.setGroupId(100);
        assertEquals(100, testEntity.getGroupId());
    }

    // Test Entity - Concrete implementation for testing BaseEntity
    @Entity
    private static class TestEntity extends BaseEntity {
        @Id
        @Column(name = "id")
        private Long id = 1L; // Default ID for test purposes

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        // Make lifecycle methods public for testing
        @Override
        public void prePersist() {
            super.prePersist();
        }

        @Override
        public void preUpdate() {
            super.preUpdate();
        }
    }
}