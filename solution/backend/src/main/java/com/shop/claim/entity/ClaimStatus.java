package com.shop.claim.entity;

/** Lifecycle status of a buyer claim (US-CLM-01, US-CLM-02). */
public enum ClaimStatus {

    /** Claim has been opened by the buyer and is awaiting a vendor decision. */
    OPEN,

    /** Vendor has made a decision (GRANTED or REFUSED) and the claim is closed. */
    CLOSED
}
