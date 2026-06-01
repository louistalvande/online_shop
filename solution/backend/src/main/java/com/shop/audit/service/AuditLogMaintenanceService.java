package com.shop.audit.service;

import com.shop.audit.dto.AuditPartitionStats;

import java.util.List;

/**
 * Manages the lifecycle of audit log partitions:
 * monthly partition creation and 13-month auto-purge (US-SEC-06 / FS-S07 / CPA-13).
 */
public interface AuditLogMaintenanceService {

    /**
     * Creates the partition for the given year/month if it does not already exist,
     * then drops (or detaches) partitions older than 13 months.
     * Designed to be called monthly by the scheduler.
     */
    void runMaintenance();

    /**
     * Returns integrity statistics for every child partition of the {@code audit_log} table.
     * Used by the admin partition-stats endpoint to verify partition health (US-SEC-06).
     *
     * @return one entry per partition, sorted by partition name
     */
    List<AuditPartitionStats> listPartitionStats();
}
