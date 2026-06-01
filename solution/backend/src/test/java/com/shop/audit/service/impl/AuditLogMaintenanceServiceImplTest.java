package com.shop.audit.service.impl;

import com.shop.audit.repository.AuditLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link AuditLogMaintenanceServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class AuditLogMaintenanceServiceImplTest {

    @Mock AuditLogRepository auditLogRepository;
    @Mock EntityManager entityManager;
    @Mock Query nativeQuery;

    AuditLogMaintenanceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuditLogMaintenanceServiceImpl(auditLogRepository, entityManager);
    }

    /** runMaintenance creates next-month partition and calls deleteByOccurredAtBefore. */
    @Test
    void runMaintenance_createsPartitionAndPurgesOldRows() {
        given(entityManager.createNativeQuery(anyString())).willReturn(nativeQuery);
        given(nativeQuery.executeUpdate()).willReturn(0);
        given(auditLogRepository.deleteByOccurredAtBefore(any())).willReturn(0L);

        service.runMaintenance();

        then(entityManager).should().createNativeQuery(anyString());
        then(auditLogRepository).should().deleteByOccurredAtBefore(any());
    }

    /** listPartitionStats returns an empty list when no partitions exist. */
    @Test
    void listPartitionStats_returnsEmptyListWhenNoRows() {
        given(entityManager.createNativeQuery(anyString())).willReturn(nativeQuery);
        given(nativeQuery.getResultList()).willReturn(List.of());

        var stats = service.listPartitionStats();

        assertThat(stats).isEmpty();
    }

    /** listPartitionStats maps raw Object[] rows to AuditPartitionStats DTOs. */
    @Test
    void listPartitionStats_mapsRowsToDto() {
        Object[] row = {"audit_log_2026_05", 42L, 8192L};
        given(entityManager.createNativeQuery(anyString())).willReturn(nativeQuery);
        given(nativeQuery.getResultList()).willReturn(List.of(row));

        var stats = service.listPartitionStats();

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getPartitionName()).isEqualTo("audit_log_2026_05");
        assertThat(stats.get(0).getRowCount()).isEqualTo(42L);
        assertThat(stats.get(0).getSizeBytes()).isEqualTo(8192L);
    }
}
