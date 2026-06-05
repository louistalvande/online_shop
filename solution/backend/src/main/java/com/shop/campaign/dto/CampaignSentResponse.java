package com.shop.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Response returned after a campaign has been sent. */
public class CampaignSentResponse {

    /** Generated campaign identifier. */
    @Schema(description = "Campaign identifier")
    private UUID id;

    /** Number of consenting buyers who received the email. */
    @Schema(description = "Number of recipients who received the email")
    private int recipientCount;

    /** Timestamp when the send was triggered. */
    @Schema(description = "UTC timestamp when the campaign was dispatched")
    private OffsetDateTime sentAt;

    /** @return the campaign id */
    public UUID getId() { return id; }

    /** @param id campaign id */
    public void setId(UUID id) { this.id = id; }

    /** @return recipient count */
    public int getRecipientCount() { return recipientCount; }

    /** @param recipientCount number of recipients */
    public void setRecipientCount(int recipientCount) { this.recipientCount = recipientCount; }

    /** @return send timestamp */
    public OffsetDateTime getSentAt() { return sentAt; }

    /** @param sentAt send timestamp */
    public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }
}
