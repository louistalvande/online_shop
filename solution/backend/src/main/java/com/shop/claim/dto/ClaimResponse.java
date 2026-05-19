package com.shop.claim.dto;

import com.shop.claim.entity.Claim;
import com.shop.claim.entity.ClaimDecision;
import com.shop.claim.entity.ClaimReason;
import com.shop.claim.entity.ClaimStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/** Read-only representation of a claim (US-CLM-01, US-CLM-02). */
public class ClaimResponse {

    @Schema(description = "Claim UUID")
    private UUID id;

    @Schema(description = "UUID of the order this claim is filed against")
    private UUID orderId;

    @Schema(description = "Human-readable order number")
    private String orderNumber;

    @Schema(description = "UUID of the buyer who opened the claim")
    private UUID buyerId;

    @Schema(description = "UUID of the vendor responsible for this claim")
    private UUID vendorId;

    @Schema(description = "Reason selected by the buyer")
    private ClaimReason reason;

    @Schema(description = "Free-text message from the buyer")
    private String message;

    @Schema(description = "Current claim status")
    private ClaimStatus status;

    @Schema(description = "Vendor decision — null while claim is OPEN")
    private ClaimDecision decision;

    @Schema(description = "Timestamp when the claim was opened")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last status change")
    private LocalDateTime updatedAt;

    private ClaimResponse() {}

    /**
     * Builds a {@link ClaimResponse} from the given {@link Claim} entity.
     *
     * @param claim the source entity
     * @return the corresponding response DTO
     */
    public static ClaimResponse from(Claim claim) {
        ClaimResponse r = new ClaimResponse();
        r.id = claim.getId();
        r.orderId = claim.getOrderId();
        r.orderNumber = claim.getOrderNumber();
        r.buyerId = claim.getBuyerId();
        r.vendorId = claim.getVendorId();
        r.reason = claim.getReason();
        r.message = claim.getMessage();
        r.status = claim.getStatus();
        r.decision = claim.getDecision();
        r.createdAt = claim.getCreatedAt();
        r.updatedAt = claim.getUpdatedAt();
        return r;
    }

    /** @return the claim UUID */
    public UUID getId() { return id; }

    /** @return the order UUID */
    public UUID getOrderId() { return orderId; }

    /** @return the human-readable order number */
    public String getOrderNumber() { return orderNumber; }

    /** @return the buyer account UUID */
    public UUID getBuyerId() { return buyerId; }

    /** @return the vendor account UUID */
    public UUID getVendorId() { return vendorId; }

    /** @return the claim reason */
    public ClaimReason getReason() { return reason; }

    /** @return the free-text message from the buyer */
    public String getMessage() { return message; }

    /** @return the current claim status */
    public ClaimStatus getStatus() { return status; }

    /** @return the vendor decision, or {@code null} if still OPEN */
    public ClaimDecision getDecision() { return decision; }

    /** @return the claim creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @return the last status-change timestamp */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
