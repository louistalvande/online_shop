package com.shop.auth.exception;

/** Thrown when an activation token is not found — it may have already been used (CS-07). */
public class TokenNotFoundException extends RuntimeException {

    /** Constructs the exception. */
    public TokenNotFoundException() {
        super("Activation token not found");
    }
}
