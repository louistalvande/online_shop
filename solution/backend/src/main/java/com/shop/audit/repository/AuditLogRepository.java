package com.shop.audit.repository;

import com.shop.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Persistence interface for {@link AuditLog} records.
 * {@link JpaSpecificationExecutor} enables dynamic filtered queries for the audit log viewer (US-SEC-05).
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>,
                                             JpaSpecificationExecutor<AuditLog> {
}
