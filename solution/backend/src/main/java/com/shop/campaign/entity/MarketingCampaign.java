package com.shop.campaign.entity;

import com.shop.account.entity.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Persistent record of one marketing campaign sent to consenting buyers (US-MKTG-01 / FS-V17). */
@Entity
@Table(name = "marketing_campaigns")
public class MarketingCampaign {

    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Vendor who triggered the campaign. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Account vendor;

    /** Email subject supplied by the vendor. */
    @Column(nullable = false, length = 200)
    private String subject;

    /** Plain-text body supplied by the vendor. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /** Number of consenting buyers who received the email. */
    @Column(nullable = false)
    private int recipientCount;

    /** Outcome of the send operation. */
    @Column(nullable = false, length = 20)
    private String status;

    /** Timestamp when the campaign was dispatched. */
    @Column(nullable = false)
    private OffsetDateTime sentAt;

    /** @return the campaign primary key */
    public UUID getId() { return id; }

    /** @return the vendor who sent the campaign */
    public Account getVendor() { return vendor; }

    /** @param vendor the vendor account */
    public void setVendor(Account vendor) { this.vendor = vendor; }

    /** @return the email subject */
    public String getSubject() { return subject; }

    /** @param subject the email subject */
    public void setSubject(String subject) { this.subject = subject; }

    /** @return the email body */
    public String getBody() { return body; }

    /** @param body the email body */
    public void setBody(String body) { this.body = body; }

    /** @return number of recipients */
    public int getRecipientCount() { return recipientCount; }

    /** @param recipientCount number of recipients */
    public void setRecipientCount(int recipientCount) { this.recipientCount = recipientCount; }

    /** @return send status */
    public String getStatus() { return status; }

    /** @param status send status */
    public void setStatus(String status) { this.status = status; }

    /** @return when the campaign was sent */
    public OffsetDateTime getSentAt() { return sentAt; }

    /** @param sentAt dispatch timestamp */
    public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }
}
