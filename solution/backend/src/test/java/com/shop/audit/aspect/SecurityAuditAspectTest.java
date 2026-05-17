package com.shop.audit.aspect;

import com.shop.account.dto.AccountResponse;
import com.shop.account.entity.Account;
import com.shop.account.entity.AccountLanguage;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import com.shop.audit.entity.AuditEventType;
import com.shop.audit.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.BDDMockito.*;

/** Unit tests for {@link SecurityAuditAspect}. */
@ExtendWith(MockitoExtension.class)
class SecurityAuditAspectTest {

    @Mock AuditLogService auditLogService;

    SecurityAuditAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new SecurityAuditAspect(auditLogService);
    }

    /** afterDeleteAccount must log ACCOUNT_DELETED with the account ID as details. */
    @Test
    void afterDeleteAccount_logsAccountDeleted() {
        UUID id = UUID.randomUUID();

        aspect.afterDeleteAccount(id);

        then(auditLogService).should().log(
                AuditEventType.ACCOUNT_DELETED,
                null,
                "accountId=" + id);
    }

    /** afterSuspendAccount must log ACCOUNT_SUSPENDED with the account ID as details. */
    @Test
    void afterSuspendAccount_logsAccountSuspended() {
        UUID id = UUID.randomUUID();

        aspect.afterSuspendAccount(id);

        then(auditLogService).should().log(
                AuditEventType.ACCOUNT_SUSPENDED,
                null,
                "accountId=" + id);
    }

    /** afterReactivateAccount must log ACCOUNT_REACTIVATED with the account ID as details. */
    @Test
    void afterReactivateAccount_logsAccountReactivated() {
        UUID id = UUID.randomUUID();

        aspect.afterReactivateAccount(id);

        then(auditLogService).should().log(
                AuditEventType.ACCOUNT_REACTIVATED,
                null,
                "accountId=" + id);
    }

    /** afterCreateAccount must log ACCOUNT_CREATED and extract the email from the DTO. */
    @Test
    void afterCreateAccount_logsAccountCreated_withEmail() {
        AccountResponse response = accountResponse("new@example.com");

        aspect.afterCreateAccount(response);

        then(auditLogService).should().log(
                AuditEventType.ACCOUNT_CREATED,
                "new@example.com",
                null);
    }

    /** afterCreateAccount must log ACCOUNT_CREATED with null email when result has no getEmail. */
    @Test
    void afterCreateAccount_logsAccountCreated_withNullEmail_whenResultIsNull() {
        aspect.afterCreateAccount(null);

        then(auditLogService).should().log(AuditEventType.ACCOUNT_CREATED, null, null);
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private AccountResponse accountResponse(String email) {
        Account a = new Account();
        a.setEmail(email);
        a.setFirstName("Alice");
        a.setLastName("Smith");
        a.setRole(AccountRole.BUYER);
        a.setStatus(AccountStatus.ACTIVE);
        a.setLanguage(AccountLanguage.FR);
        return AccountResponse.from(a);
    }
}
