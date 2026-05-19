package com.shop.claim.exception;

import com.shop.claim.entity.ClaimStatus;

import java.util.UUID;

/** Thrown when an operation is attempted on a claim that is not in the required state. */
public class InvalidClaimStateException extends RuntimeException {

    /**
     * @param claimId the UUID of the claim in the wrong state
     * @param current the current status of the claim
     */
    public InvalidClaimStateException(UUID claimId, ClaimStatus current) {
        super("Claim " + claimId + " is in state " + current + " — operation not allowed");
    }
}
