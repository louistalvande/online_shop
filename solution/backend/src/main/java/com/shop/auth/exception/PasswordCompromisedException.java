package com.shop.auth.exception;

/** Thrown when a candidate password appears in the HIBP compromised-password database (SEC-PWD-002 / CPA-16). */
public class PasswordCompromisedException extends RuntimeException {

    /** Constructs the exception. */
    public PasswordCompromisedException() {
        super("Password found in known data breaches");
    }
}
