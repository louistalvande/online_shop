package com.shop.audit.service.impl;

import com.shop.audit.dto.AuditPartitionStats;
import com.shop.audit.repository.AuditLogRepository;
import com.shop.audit.service.AuditLogMaintenanceService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages audit log partition lifecycle: monthly creation of the next partition and
 * purge of partitions older than 13 months (US-SEC-06 / FS-S07 / CPA-13).
 */
@Service
public class AuditLogMaintenanceServiceImpl implements AuditLogMaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogMaintenanceServiceImpl.class);
    private static final int RETENTION_MONTHS = 13;

    private final AuditLogRepository auditLogRepository;
    private final EntityManager entityManager;

    /**
     * @param auditLogRepository used for the delete-before-threshold purge
     * @param entityManager      used for native DDL (CREATE TABLE IF NOT EXISTS ... PARTITION OF)
     */
    public AuditLogMaintenanceServiceImpl(AuditLogRepository auditLogRepository,
                                           EntityManager entityManager) {
        this.auditLogRepository = auditLogRepository;
        this.entityManager      = entityManager;
    }

    /**
     * {@inheritDoc}
     * Runs at 02:00 on the 1st of every month.
     */
    @Override
    @Scheduled(cron = "0 0 2 1 * *")
    @Transactional
    public void runMaintenance() {
        LocalDate now = LocalDate.now(ZoneOffset.UTC);

        // Create the partition for next month so inserts are always landing on a named partition
        createPartitionIfAbsent(now.plusMonths(1));

        // Purge rows older than 13 months (DELETE, not DROP — allows the scheduler to run as the app user)
        LocalDate cutoff = now.minusMonths(RETENTION_MONTHS);
        long deleted = auditLogRepository.deleteByOccurredAtBefore(
                cutoff.atStartOfDay(ZoneOffset.UTC).toInstant());

        if (deleted > 0) {
            log.info("[SEC-06] Audit log maintenance: deleted {} entries older than {} ({})",
                    deleted, RETENTION_MONTHS + " months", cutoff);
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<AuditPartitionStats> listPartitionStats() {
        // Query pg_inherits + pg_class to list partitions of audit_log with size estimates
        String sql = """
                SELECT
                    child.relname                              AS partition_name,
                    pg_stat_get_live_tuples(child.oid)        AS row_count,
                    pg_total_relation_size(child.oid)         AS size_bytes
                FROM pg_inherits
                JOIN pg_class parent ON pg_inherits.inhparent = parent.oid
                JOIN pg_class child  ON pg_inherits.inhrelid  = child.oid
                WHERE parent.relname = 'audit_log'
                ORDER BY child.relname
                """;

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> rows = query.getResultList();

        List<AuditPartitionStats> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            result.add(new AuditPartitionStats(
                    (String) row[0],
                    toLong(row[1]),
                    toLong(row[2])));
        }
        return result;
    }

    /**
     * Creates a named monthly partition if it does not already exist (idempotent).
     *
     * @param month the month for which to create the partition
     */
    private void createPartitionIfAbsent(LocalDate month) {
        LocalDate start = month.withDayOfMonth(1);
        LocalDate end   = start.plusMonths(1);
        String name = "audit_log_" + start.format(DateTimeFormatter.ofPattern("yyyy_MM"));

        String ddl = String.format(
                "CREATE TABLE IF NOT EXISTS %s PARTITION OF audit_log " +
                "FOR VALUES FROM ('%s') TO ('%s')",
                name, start, end);

        entityManager.createNativeQuery(ddl).executeUpdate();
        log.debug("[SEC-06] Ensured partition {} exists", name);
    }

    private static long toLong(Object value) {
        if (value instanceof Number n) return n.longValue();
        return 0L;
    }
}
