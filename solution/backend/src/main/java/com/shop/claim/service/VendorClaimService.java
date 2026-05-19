package com.shop.claim.service;

import com.shop.claim.dto.ClaimResponse;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Vendor-facing claim management operations (US-CLM-02). */
public interface VendorClaimService {

    /**
     * Returns all claims for the authenticated vendor, newest first.
     *
     * @param vendorEmail the authenticated vendor's email
     * @return list of claims
     */
    List<ClaimResponse> getVendorClaims(String vendorEmail);

    /**
     * Returns a specific claim belonging to the authenticated vendor.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param claimId     the claim UUID
     * @return the matching claim
     * @throws com.shop.claim.exception.ClaimNotFoundException if not found or not owned by the vendor
     */
    ClaimResponse getVendorClaim(String vendorEmail, UUID claimId);

    /**
     * Grants a refund for the specified claim.
     * For CARD orders, triggers a Stripe refund. For WIRE orders, closes the claim without
     * an automatic refund (the vendor handles the bank transfer manually).
     * The claim transitions to CLOSED with decision GRANTED.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param claimId     the claim UUID
     * @param locale      locale for buyer notification email
     * @return the updated claim
     * @throws com.shop.claim.exception.ClaimNotFoundException    if not found or not owned
     * @throws com.shop.claim.exception.InvalidClaimStateException if claim is not OPEN
     */
    ClaimResponse grantRefund(String vendorEmail, UUID claimId, Locale locale);

    /**
     * Refuses a refund for the specified claim.
     * The claim transitions to CLOSED with decision REFUSED. The buyer is notified.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param claimId     the claim UUID
     * @param locale      locale for buyer notification email
     * @return the updated claim
     * @throws com.shop.claim.exception.ClaimNotFoundException    if not found or not owned
     * @throws com.shop.claim.exception.InvalidClaimStateException if claim is not OPEN
     */
    ClaimResponse refuseRefund(String vendorEmail, UUID claimId, Locale locale);
}
