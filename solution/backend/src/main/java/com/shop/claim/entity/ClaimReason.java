package com.shop.claim.entity;

/** Reason selected by the buyer when opening a claim (US-CLM-01). */
public enum ClaimReason {

    /** Order was never received by the buyer. */
    NON_RECEIPT,

    /** Item was received but is defective or damaged. */
    DEFECTIVE_ITEM,

    /** Item received does not match the ordered product. */
    WRONG_ITEM,

    /** Any other issue not covered by the specific reasons above. */
    OTHER
}
