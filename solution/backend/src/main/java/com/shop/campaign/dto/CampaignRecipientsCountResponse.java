package com.shop.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response for the recipients count endpoint (GET /api/vendor/campaigns/recipients/count). */
public class CampaignRecipientsCountResponse {

    /** Number of active buyers who have opted in to marketing emails. */
    @Schema(description = "Number of active buyers with marketing consent")
    private int count;

    /** @return the count */
    public int getCount() { return count; }

    /** @param count the count */
    public void setCount(int count) { this.count = count; }
}
