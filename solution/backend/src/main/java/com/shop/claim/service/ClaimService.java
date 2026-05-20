package com.shop.claim.service;

import com.shop.claim.dto.ClaimResponse;
import com.shop.claim.dto.CreateClaimRequest;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Buyer-facing claim lifecycle operations (US-CLM-01). */
public interface ClaimService {

    /**
     * Opens a new claim for the specified order.
     * The order must belong to the authenticated buyer and be in a claimable state.
     * Only one OPEN claim per order is allowed at a time.
     *
     * @param buyerEmail the authenticated buyer's email
     * @param orderId    the UUID of the order being claimed
     * @param request    the claim reason and message
     * @param locale     locale for vendor notification email
     * @return the created claim
     */
    ClaimResponse openClaim(String buyerEmail, UUID orderId, CreateClaimRequest request, Locale locale);

    /**
     * Returns all claims opened by the authenticated buyer, newest first.
     *
     * @param buyerEmail the authenticated buyer's email
     * @return list of claims
     */
    List<ClaimResponse> getMyClaims(String buyerEmail);

    /**
     * Returns a specific claim owned by the authenticated buyer.
     *
     * @param buyerEmail the authenticated buyer's email
     * @param claimId    the claim UUID
     * @return the matching claim
     * @throws com.shop.claim.exception.ClaimNotFoundException if not found or not owned by the buyer
     */
    ClaimResponse getMyClaim(String buyerEmail, UUID claimId);
}
