package com.shop.auth.exception;

/** Thrown when login credentials are invalid or the account is not active. */
public class InvalidCredentialsException extends RuntimeException {

    /** Constructs the exception with a generic message (not exposed to the client). */
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
