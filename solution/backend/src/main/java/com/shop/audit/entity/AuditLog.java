package com.shop.audit.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Immutable record of a security-relevant event (SEC-LOG-001 / CPA-13). */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    /** Database-generated identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Type of security event. */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AuditEventType eventType;

    /** Email address of the account involved, if known. */
    @Column(length = 255)
    private String email;

    /** IP address of the request originator (IPv4 or IPv6). */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** Optional free-text context (e.g. failure reason). */
    @Column(columnDefinition = "TEXT")
    private String details;

    /** UTC timestamp of the event. */
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    /**
     * Sets the timestamp before persisting.
     * Ensures {@code occurred_at} is always populated even if the caller omits it.
     */
    @PrePersist
    void prePersist() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    /** @return the database identifier */
    public Long getId() { return id; }

    /** @return the event type */
    public AuditEventType getEventType() { return eventType; }

    /** @param eventType the event type to set */
    public void setEventType(AuditEventType eventType) { this.eventType = eventType; }

    /** @return the email address, or {@code null} if not applicable */
    public String getEmail() { return email; }

    /** @param email the email to set */
    public void setEmail(String email) { this.email = email; }

    /** @return the originating IP address */
    public String getIpAddress() { return ipAddress; }

    /** @param ipAddress the IP address to set */
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    /** @return optional event details */
    public String getDetails() { return details; }

    /** @param details the details to set */
    public void setDetails(String details) { this.details = details; }

    /** @return the UTC instant the event occurred */
    public Instant getOccurredAt() { return occurredAt; }

    /** @param occurredAt the timestamp to set */
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
