package com.shop.audit.repository;

import com.shop.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;

/**
 * Persistence interface for {@link AuditLog} records.
 * {@link JpaSpecificationExecutor} enables dynamic filtered queries for the audit log viewer (US-SEC-05).
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>,
                                             JpaSpecificationExecutor<AuditLog> {

    /**
     * Deletes all audit log entries older than the given timestamp.
     * Used by the 13-month auto-purge scheduled job (US-SEC-06 / CPA-13).
     *
     * @param threshold entries with {@code occurred_at} strictly before this instant are deleted
     * @return the number of deleted rows
     */
    long deleteByOccurredAtBefore(Instant threshold);
}
