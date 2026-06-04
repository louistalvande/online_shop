package com.shop.campaign.service;

import com.shop.campaign.dto.CampaignRecipientsCountResponse;
import com.shop.campaign.dto.CampaignSentResponse;
import com.shop.campaign.dto.SendCampaignRequest;

/** Business operations for marketing campaigns (US-MKTG-01 / FS-V17). */
public interface CampaignService {

    /**
     * Returns the number of active buyers who have opted in to marketing emails.
     * Used by the frontend to display the recipient count before sending.
     *
     * @param vendorEmail the authenticated vendor's email (for audit purposes)
     * @return the recipient count
     */
    CampaignRecipientsCountResponse getRecipientsCount(String vendorEmail);

    /**
     * Sends a promotional email campaign to all active buyers with marketing consent.
     * Persists a {@code MarketingCampaign} log entry regardless of outcome.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param request     the campaign subject and body
     * @return summary of the sent campaign (id, recipient count, timestamp)
     * @throws com.shop.campaign.exception.NoConsentingBuyersException if no eligible buyers exist
     */
    CampaignSentResponse sendCampaign(String vendorEmail, SendCampaignRequest request);
}
