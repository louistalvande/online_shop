package com.shop.audit.aspect;

import com.shop.audit.entity.AuditEventType;
import com.shop.audit.service.AuditLogService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Cross-cutting audit concern — intercepts admin account operations and persists
 * security-relevant events to the audit log (CS-15 / CPA-13 / SEC-LOG-001..003).
 *
 * <p>Auth events (login, registration, activation, password change) are logged
 * directly inside {@link com.shop.auth.service.impl.AuthServiceImpl} because
 * the email and outcome context needed for the audit record is already available there.
 * This aspect covers the remaining admin-initiated account lifecycle events:
 *
 * <ul>
 *   <li>{@code AccountServiceImpl.createAccount}   — ACCOUNT_CREATED</li>
 *   <li>{@code AccountServiceImpl.deleteAccount}   — ACCOUNT_DELETED</li>
 *   <li>{@code AccountServiceImpl.suspendAccount}  — ACCOUNT_SUSPENDED</li>
 *   <li>{@code AccountServiceImpl.reactivateAccount} — ACCOUNT_REACTIVATED</li>
 * </ul>
 */
@Aspect
@Component
public class SecurityAuditAspect {

    private final AuditLogService auditLogService;

    /**
     * Constructs the aspect with the audit log service.
     *
     * @param auditLogService the service that persists audit records
     */
    public SecurityAuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Logs an {@code ACCOUNT_CREATED} event after a successful admin account creation.
     * The email is extracted reflectively from the returned DTO.
     *
     * @param result the DTO returned by the service method
     */
    @AfterReturning(
        pointcut = "execution(* com.shop.account.service.impl.AccountServiceImpl.createAccount(..))",
        returning = "result")
    public void afterCreateAccount(Object result) {
        auditLogService.log(AuditEventType.ACCOUNT_CREATED, extractEmail(result), null);
    }

    /**
     * Logs an {@code ACCOUNT_DELETED} event after a successful delete operation.
     *
     * @param accountId the UUID of the deleted account
     */
    @AfterReturning(
        pointcut = "execution(* com.shop.account.service.impl.AccountServiceImpl.deleteAccount(..)) && args(accountId)",
        argNames = "accountId")
    public void afterDeleteAccount(UUID accountId) {
        auditLogService.log(AuditEventType.ACCOUNT_DELETED, null, "accountId=" + accountId);
    }

    /**
     * Logs an {@code ACCOUNT_SUSPENDED} event after a successful suspend operation.
     *
     * @param accountId the UUID of the suspended account
     */
    @AfterReturning(
        pointcut = "execution(* com.shop.account.service.impl.AccountServiceImpl.suspendAccount(..)) && args(accountId)",
        argNames = "accountId")
    public void afterSuspendAccount(UUID accountId) {
        auditLogService.log(AuditEventType.ACCOUNT_SUSPENDED, null, "accountId=" + accountId);
    }

    /**
     * Logs an {@code ACCOUNT_REACTIVATED} event after a successful reactivation.
     *
     * @param accountId the UUID of the reactivated account
     */
    @AfterReturning(
        pointcut = "execution(* com.shop.account.service.impl.AccountServiceImpl.reactivateAccount(..)) && args(accountId)",
        argNames = "accountId")
    public void afterReactivateAccount(UUID accountId) {
        auditLogService.log(AuditEventType.ACCOUNT_REACTIVATED, null, "accountId=" + accountId);
    }

    /**
     * Reflectively extracts an {@code email} value from a DTO via {@code getEmail()}.
     * Returns {@code null} when the object is null or has no accessible email getter.
     *
     * @param obj the DTO to inspect
     * @return the email string, or {@code null}
     */
    private String extractEmail(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return (String) obj.getClass().getMethod("getEmail").invoke(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
