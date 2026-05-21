package com.shop.auth.exception;

/** Thrown when a password-reset token is missing, expired, or already used (SEC-PWD-006 / CPA-17). */
public class InvalidResetTokenException extends RuntimeException {

    /** Constructs the exception. */
    public InvalidResetTokenException() {
        super("Password reset token is invalid or expired");
    }
}
