package com.shop.campaign.exception;

/** Thrown when a campaign send is attempted but no active buyer has given marketing consent. */
public class NoConsentingBuyersException extends RuntimeException {

    /** Creates the exception with a default message. */
    public NoConsentingBuyersException() {
        super("No active buyers with marketing consent found");
    }
}
