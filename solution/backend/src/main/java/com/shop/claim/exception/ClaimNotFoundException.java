package com.shop.claim.exception;

import java.util.UUID;

/** Thrown when a claim is not found or does not belong to the requesting user. */
public class ClaimNotFoundException extends RuntimeException {

    /**
     * @param claimId the UUID of the claim that was not found
     */
    public ClaimNotFoundException(UUID claimId) {
        super("Claim not found: " + claimId);
    }
}
