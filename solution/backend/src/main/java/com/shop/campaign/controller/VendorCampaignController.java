package com.shop.campaign.controller;

import com.shop.campaign.dto.CampaignRecipientsCountResponse;
import com.shop.campaign.dto.CampaignSentResponse;
import com.shop.campaign.dto.SendCampaignRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

/** Vendor endpoints for marketing email campaigns (US-MKTG-01 / FS-V17). */
@Tag(name = "Vendor — Campaigns", description = "Marketing email campaign endpoints for authenticated vendors")
@RequestMapping("/api/vendor/campaigns")
public interface VendorCampaignController {

    /**
     * Returns the number of active buyers who have opted in to marketing emails.
     *
     * @param principal the authenticated vendor
     * @return recipient count
     */
    @GetMapping("/recipients/count")
    @Operation(summary = "Get number of consenting buyers")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Count returned"),
        @ApiResponse(responseCode = "403", description = "Not a vendor account")
    })
    ResponseEntity<CampaignRecipientsCountResponse> getRecipientsCount(Principal principal);

    /**
     * Sends a promotional email campaign to all active buyers with marketing consent.
     *
     * @param principal the authenticated vendor
     * @param request   campaign subject and body
     * @return summary of the sent campaign
     */
    @PostMapping("/send")
    @Operation(summary = "Send a marketing email campaign to consenting buyers")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Campaign sent"),
        @ApiResponse(responseCode = "400", description = "No consenting buyers (NO_CONSENTING_BUYERS) or validation error"),
        @ApiResponse(responseCode = "403", description = "Not a vendor account")
    })
    ResponseEntity<CampaignSentResponse> sendCampaign(Principal principal,
                                                       @RequestBody @Valid SendCampaignRequest request);
}
