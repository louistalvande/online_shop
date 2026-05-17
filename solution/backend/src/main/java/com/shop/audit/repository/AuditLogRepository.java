package com.shop.audit.repository;

import com.shop.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence interface for {@link AuditLog} records. */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
