package com.shop.claim.dto;

import com.shop.claim.entity.ClaimReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request body for opening a claim against an order (US-CLM-01). */
public class CreateClaimRequest {

    @Schema(description = "Reason for the claim", example = "NON_RECEIPT")
    @NotNull
    private ClaimReason reason;

    @Schema(description = "Free-text description of the issue", example = "My order never arrived.")
    @NotBlank
    private String message;

    /** @return the selected claim reason */
    public ClaimReason getReason() { return reason; }

    /** @param reason the selected claim reason */
    public void setReason(ClaimReason reason) { this.reason = reason; }

    /** @return the free-text message from the buyer */
    public String getMessage() { return message; }

    /** @param message the free-text message */
    public void setMessage(String message) { this.message = message; }
}
