package com.shop.audit.controller;

import com.shop.audit.dto.AuditLogPageResponse;
import com.shop.audit.dto.AuditPartitionStats;
import com.shop.audit.entity.AuditEventType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.List;

/**
 * Admin-only endpoints for querying and exporting the security audit log (US-SEC-05 / FS-S07 / CPA-13).
 */
@Tag(name = "Admin — Audit Log", description = "Security audit log — admin access only (US-SEC-05)")
@RequestMapping("/api/admin/audit-logs")
public interface AuditLogController {

    /**
     * Returns a paginated, filtered view of the audit log (US-SEC-05).
     * All filter parameters are optional; results are sorted by {@code occurredAt} descending.
     *
     * @param eventType exact event type filter
     * @param email     email substring filter (case-insensitive)
     * @param ipAddress exact IP address filter
     * @param from      inclusive start of the time range (ISO-8601)
     * @param to        inclusive end of the time range (ISO-8601)
     * @param page      zero-based page index (default 0)
     * @param size      page size (default 20, max 200)
     * @return paginated audit log entries
     */
    @Operation(summary = "Query audit log with optional filters (US-SEC-05)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paginated results returned"),
        @ApiResponse(responseCode = "403", description = "Not an ADMIN")
    })
    @GetMapping
    ResponseEntity<AuditLogPageResponse> queryLogs(
            @RequestParam(required = false) AuditEventType eventType,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size);

    /**
     * Exports filtered audit log entries as a CSV file (US-SEC-05).
     * All filter parameters are optional. Results sorted by {@code occurredAt} descending.
     *
     * @param eventType exact event type filter
     * @param email     email substring filter (case-insensitive)
     * @param ipAddress exact IP address filter
     * @param from      inclusive start of the time range (ISO-8601)
     * @param to        inclusive end of the time range (ISO-8601)
     * @return CSV file attachment
     */
    @Operation(summary = "Export filtered audit log as CSV (US-SEC-05)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV file returned"),
        @ApiResponse(responseCode = "403", description = "Not an ADMIN")
    })
    @GetMapping(value = "/export", produces = "text/csv")
    ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) AuditEventType eventType,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to);

    /**
     * Returns integrity statistics for every child partition of the audit log table (US-SEC-06).
     * Allows administrators to verify partition health and estimated row counts.
     *
     * @return list of partitions with row count and disk size
     */
    @Operation(summary = "List audit log partition statistics (US-SEC-06)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Partition list returned"),
        @ApiResponse(responseCode = "403", description = "Not an ADMIN")
    })
    @GetMapping("/partitions")
    ResponseEntity<List<AuditPartitionStats>> listPartitions();
}
