package com.shop.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Integrity statistics for a single audit log partition (US-SEC-06 / CPA-13). */
public class AuditPartitionStats {

    @Schema(description = "Partition table name, e.g. audit_log_2026_05") private String partitionName;
    @Schema(description = "Number of rows in this partition") private long rowCount;
    @Schema(description = "Estimated total size on disk (bytes)") private long sizeBytes;

    /**
     * @param partitionName the partition table name
     * @param rowCount      number of rows
     * @param sizeBytes     estimated disk size in bytes
     */
    public AuditPartitionStats(String partitionName, long rowCount, long sizeBytes) {
        this.partitionName = partitionName;
        this.rowCount      = rowCount;
        this.sizeBytes     = sizeBytes;
    }

    /** @return the partition table name */
    public String getPartitionName() { return partitionName; }
    /** @return number of rows in the partition */
    public long getRowCount() { return rowCount; }
    /** @return estimated disk size in bytes */
    public long getSizeBytes() { return sizeBytes; }
}
