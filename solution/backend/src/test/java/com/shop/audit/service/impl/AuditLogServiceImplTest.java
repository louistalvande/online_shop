package com.shop.audit.service.impl;

import com.shop.audit.entity.AuditEventType;
import com.shop.audit.entity.AuditLog;
import com.shop.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

/** Unit tests for {@link AuditLogServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock AuditLogRepository auditLogRepository;

    AuditLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuditLogServiceImpl(auditLogRepository);
    }

    /** log must persist an AuditLog entry with the supplied fields. */
    @Test
    void log_persistsEntryWithCorrectFields() {
        service.log(AuditEventType.LOGIN_SUCCESS, "alice@example.com", "details");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        then(auditLogRepository).should().save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getEventType()).isEqualTo(AuditEventType.LOGIN_SUCCESS);
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getDetails()).isEqualTo("details");
    }

    /** log must persist an entry with null email and details when not provided. */
    @Test
    void log_acceptsNullEmailAndDetails() {
        service.log(AuditEventType.ACCOUNT_LOCKED, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        then(auditLogRepository).should().save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getEventType()).isEqualTo(AuditEventType.ACCOUNT_LOCKED);
        assertThat(saved.getEmail()).isNull();
        assertThat(saved.getDetails()).isNull();
    }

    /** log must call save exactly once per invocation. */
    @Test
    void log_callsSaveExactlyOnce() {
        service.log(AuditEventType.LOGIN_FAILURE, "bob@example.com", null);

        then(auditLogRepository).should(times(1)).save(any(AuditLog.class));
    }
}
