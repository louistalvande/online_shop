package com.shop.claim.controller.impl;

import com.shop.claim.controller.ClaimController;
import com.shop.claim.dto.ClaimResponse;
import com.shop.claim.dto.CreateClaimRequest;
import com.shop.claim.service.ClaimService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Buyer-facing claim controller (US-CLM-01). */
@RestController
public class ClaimControllerImpl implements ClaimController {

    private final ClaimService claimService;

    /**
     * @param claimService the buyer claim service
     */
    public ClaimControllerImpl(ClaimService claimService) {
        this.claimService = claimService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ClaimResponse> openClaim(
            UUID orderId, CreateClaimRequest request, Principal principal, Locale locale) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(claimService.openClaim(principal.getName(), orderId, request, locale));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<ClaimResponse>> getOrderClaims(UUID orderId, Principal principal) {
        return ResponseEntity.ok(claimService.getMyClaims(principal.getName()));
    }
}
