package com.shop.auth.exception;

/** Thrown when a login account is temporarily locked after too many failed attempts (SEC-AUTH-003). */
public class TooManyLoginAttemptsException extends RuntimeException {

    /** Constructs the exception for the given email address. */
    public TooManyLoginAttemptsException(String email) {
        super("Too many login attempts for: " + email);
    }
}
