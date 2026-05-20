package com.shop.claim.controller.impl;

import com.shop.claim.controller.VendorClaimController;
import com.shop.claim.dto.ClaimResponse;
import com.shop.claim.service.VendorClaimService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Vendor-facing claim management controller (US-CLM-02). */
@RestController
public class VendorClaimControllerImpl implements VendorClaimController {

    private final VendorClaimService vendorClaimService;

    /**
     * @param vendorClaimService the vendor claim service
     */
    public VendorClaimControllerImpl(VendorClaimService vendorClaimService) {
        this.vendorClaimService = vendorClaimService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<ClaimResponse>> listClaims(Principal principal) {
        return ResponseEntity.ok(vendorClaimService.getVendorClaims(principal.getName()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ClaimResponse> getClaim(UUID claimId, Principal principal) {
        return ResponseEntity.ok(vendorClaimService.getVendorClaim(principal.getName(), claimId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ClaimResponse> grantRefund(UUID claimId, Principal principal, Locale locale) {
        return ResponseEntity.ok(vendorClaimService.grantRefund(principal.getName(), claimId, locale));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ClaimResponse> refuseRefund(UUID claimId, Principal principal, Locale locale) {
        return ResponseEntity.ok(vendorClaimService.refuseRefund(principal.getName(), claimId, locale));
    }
}
