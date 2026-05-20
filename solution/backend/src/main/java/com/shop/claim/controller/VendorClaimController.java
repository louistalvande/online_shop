package com.shop.claim.controller;

import com.shop.claim.dto.ClaimResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** REST API for vendor claim management (US-CLM-02). */
@Tag(name = "Vendor Claims", description = "Vendor claim management")
@RequestMapping("/api/vendor/claims")
public interface VendorClaimController {

    /**
     * Returns all claims belonging to the authenticated vendor, newest first.
     *
     * @param principal authenticated vendor
     * @return 200 with list of claims
     */
    @Operation(summary = "List all claims for the authenticated vendor (US-CLM-02)")
    @ApiResponse(responseCode = "200", description = "Claims retrieved")
    @GetMapping
    ResponseEntity<List<ClaimResponse>> listClaims(Principal principal);

    /**
     * Returns a specific claim belonging to the authenticated vendor.
     *
     * @param claimId   the claim UUID
     * @param principal authenticated vendor
     * @return 200 with the claim
     */
    @Operation(summary = "Get a specific claim (US-CLM-02)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim retrieved"),
            @ApiResponse(responseCode = "404", description = "Claim not found")
    })
    @GetMapping("/{claimId}")
    ResponseEntity<ClaimResponse> getClaim(
            @PathVariable UUID claimId,
            Principal principal);

    /**
     * Grants a refund for the specified claim.
     * Triggers a Stripe refund for CARD orders; closes the claim for WIRE orders.
     *
     * @param claimId   the claim UUID
     * @param principal authenticated vendor
     * @param locale    vendor locale for buyer notification
     * @return 200 with the updated claim
     */
    @Operation(summary = "Grant a refund for a claim (US-CLM-02)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund granted, claim closed"),
            @ApiResponse(responseCode = "404", description = "Claim not found"),
            @ApiResponse(responseCode = "409", description = "Claim is not OPEN")
    })
    @PostMapping("/{claimId}/grant")
    ResponseEntity<ClaimResponse> grantRefund(
            @PathVariable UUID claimId,
            Principal principal,
            Locale locale);

    /**
     * Refuses a refund for the specified claim.
     *
     * @param claimId   the claim UUID
     * @param principal authenticated vendor
     * @param locale    vendor locale for buyer notification
     * @return 200 with the updated claim
     */
    @Operation(summary = "Refuse a refund for a claim (US-CLM-02)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund refused, claim closed"),
            @ApiResponse(responseCode = "404", description = "Claim not found"),
            @ApiResponse(responseCode = "409", description = "Claim is not OPEN")
    })
    @PostMapping("/{claimId}/refuse")
    ResponseEntity<ClaimResponse> refuseRefund(
            @PathVariable UUID claimId,
            Principal principal,
            Locale locale);
}
