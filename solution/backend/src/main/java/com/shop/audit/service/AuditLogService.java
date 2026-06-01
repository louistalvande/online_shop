package com.shop.audit.service;

import com.shop.audit.dto.AuditLogPageResponse;
import com.shop.audit.entity.AuditEventType;

import java.time.Instant;

/**
 * Records and queries security-relevant events in the persistent audit log (SEC-LOG-001 / CPA-13).
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

    /**
     * Returns a paginated and filtered view of the audit log (US-SEC-05 / FS-S07 / CPA-13).
     * All filter parameters are optional; omitting them returns all entries.
     *
     * @param eventType exact event type filter, or {@code null} for all types
     * @param email     email substring filter, or {@code null}
     * @param ipAddress exact IP address filter, or {@code null}
     * @param from      start of the time range (inclusive), or {@code null}
     * @param to        end of the time range (inclusive), or {@code null}
     * @param page      zero-based page index
     * @param size      page size (max 200)
     * @return the matching entries with pagination metadata
     */
    AuditLogPageResponse queryLogs(AuditEventType eventType, String email, String ipAddress,
                                    Instant from, Instant to, int page, int size);

    /**
     * Exports filtered audit log entries as UTF-8 CSV bytes (US-SEC-05).
     * All filter parameters are optional.
     *
     * @param eventType exact event type filter, or {@code null}
     * @param email     email substring filter, or {@code null}
     * @param ipAddress exact IP address filter, or {@code null}
     * @param from      start of the time range, or {@code null}
     * @param to        end of the time range, or {@code null}
     * @return CSV bytes with a header row followed by matching log entries
     */
    byte[] exportCsv(AuditEventType eventType, String email, String ipAddress,
                     Instant from, Instant to);
}
