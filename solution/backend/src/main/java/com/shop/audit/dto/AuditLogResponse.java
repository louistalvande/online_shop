package com.shop.audit.dto;

import com.shop.audit.entity.AuditEventType;
import com.shop.audit.entity.AuditLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Read-only view of a single audit log entry (US-SEC-05 / SEC-LOG-001 / CPA-13). */
public class AuditLogResponse {

    @Schema(description = "Database identifier") private Long id;
    @Schema(description = "Security event type") private AuditEventType eventType;
    @Schema(description = "Email address of the account involved, or null") private String email;
    @Schema(description = "Originating IP address") private String ipAddress;
    @Schema(description = "Optional free-text context") private String details;
    @Schema(description = "UTC timestamp of the event") private Instant occurredAt;

    /**
     * Builds a response DTO from an {@link AuditLog} entity.
     *
     * @param log the source entity
     * @return the populated DTO
     */
    public static AuditLogResponse from(AuditLog log) {
        AuditLogResponse r = new AuditLogResponse();
        r.id          = log.getId();
        r.eventType   = log.getEventType();
        r.email       = log.getEmail();
        r.ipAddress   = log.getIpAddress();
        r.details     = log.getDetails();
        r.occurredAt  = log.getOccurredAt();
        return r;
    }

    /** @return the database identifier */
    public Long getId() { return id; }
    /** @return the security event type */
    public AuditEventType getEventType() { return eventType; }
    /** @return the email address, or {@code null} */
    public String getEmail() { return email; }
    /** @return the originating IP address */
    public String getIpAddress() { return ipAddress; }
    /** @return optional event context */
    public String getDetails() { return details; }
    /** @return the UTC event timestamp */
    public Instant getOccurredAt() { return occurredAt; }
}
