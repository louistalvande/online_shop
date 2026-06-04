package com.shop.campaign.controller.impl;

import com.shop.campaign.controller.VendorCampaignController;
import com.shop.campaign.dto.CampaignRecipientsCountResponse;
import com.shop.campaign.dto.CampaignSentResponse;
import com.shop.campaign.dto.SendCampaignRequest;
import com.shop.campaign.service.CampaignService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/** REST implementation of {@link VendorCampaignController}. */
@RestController
@PreAuthorize("hasRole('VENDOR')")
public class VendorCampaignControllerImpl implements VendorCampaignController {

    private final CampaignService campaignService;

    /**
     * @param campaignService the campaign business service
     */
    public VendorCampaignControllerImpl(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CampaignRecipientsCountResponse> getRecipientsCount(Principal principal) {
        return ResponseEntity.ok(campaignService.getRecipientsCount(principal.getName()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CampaignSentResponse> sendCampaign(Principal principal, SendCampaignRequest request) {
        return ResponseEntity.ok(campaignService.sendCampaign(principal.getName(), request));
    }
}
