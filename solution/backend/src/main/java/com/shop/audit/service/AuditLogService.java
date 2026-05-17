package com.shop.audit.service;

import com.shop.audit.entity.AuditEventType;

/**
 * Records security-relevant events to the persistent audit log (SEC-LOG-001 / CPA-13).
 * The IP address is resolved from the current HTTP request context when available.
 */
public interface AuditLogService {

    /**
     * Persists a new audit entry.
     *
     * @param eventType the type of security event
     * @param email     the account email address involved, or {@code null} if not applicable
     * @param details   optional free-text context
     */
    void log(AuditEventType eventType, String email, String details);
}
