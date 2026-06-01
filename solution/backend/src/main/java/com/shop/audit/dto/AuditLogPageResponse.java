package com.shop.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Paginated wrapper for audit log results (US-SEC-05). */
public class AuditLogPageResponse {

    @Schema(description = "Audit log entries for the requested page") private List<AuditLogResponse> content;
    @Schema(description = "Total number of matching entries across all pages") private long totalElements;
    @Schema(description = "Total number of pages") private int totalPages;
    @Schema(description = "Zero-based current page index") private int page;
    @Schema(description = "Number of entries per page") private int size;

    /**
     * @param content       the entries for this page
     * @param totalElements total matching entries
     * @param totalPages    total pages
     * @param page          current page (0-based)
     * @param size          page size
     */
    public AuditLogPageResponse(List<AuditLogResponse> content, long totalElements,
                                 int totalPages, int page, int size) {
        this.content       = content;
        this.totalElements = totalElements;
        this.totalPages    = totalPages;
        this.page          = page;
        this.size          = size;
    }

    /** @return the entries for this page */
    public List<AuditLogResponse> getContent() { return content; }
    /** @return total matching entries */
    public long getTotalElements() { return totalElements; }
    /** @return total pages */
    public int getTotalPages() { return totalPages; }
    /** @return current page index */
    public int getPage() { return page; }
    /** @return page size */
    public int getSize() { return size; }
}
