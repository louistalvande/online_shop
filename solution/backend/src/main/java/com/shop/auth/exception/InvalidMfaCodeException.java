package com.shop.auth.exception;

/** Thrown when a TOTP code submitted during MFA verification is invalid or expired (SEC-AUTH-007 / CPA-15). */
public class InvalidMfaCodeException extends RuntimeException {

    /** Constructs the exception. */
    public InvalidMfaCodeException() {
        super("Invalid or expired MFA code");
    }
}
