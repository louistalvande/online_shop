package com.shop.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for {@code POST /api/vendor/campaigns/send}. */
public class SendCampaignRequest {

    /** Email subject (max 200 characters). */
    @NotBlank
    @Size(max = 200)
    @Schema(description = "Email subject line", example = "Summer sale — up to 30% off")
    private String subject;

    /** Plain-text body of the promotional email. */
    @NotBlank
    @Schema(description = "Email body text", example = "Dear customer, discover our latest offers…")
    private String body;

    /** @return the subject */
    public String getSubject() { return subject; }

    /** @param subject the subject */
    public void setSubject(String subject) { this.subject = subject; }

    /** @return the body */
    public String getBody() { return body; }

    /** @param body the body */
    public void setBody(String body) { this.body = body; }
}
