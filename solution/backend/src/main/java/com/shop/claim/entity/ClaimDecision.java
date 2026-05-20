package com.shop.claim.entity;

/** Vendor decision recorded when closing a claim (US-CLM-02). */
public enum ClaimDecision {

    /** Vendor agrees to issue a refund to the buyer. */
    GRANTED,

    /** Vendor refuses to issue a refund to the buyer. */
    REFUSED
}
