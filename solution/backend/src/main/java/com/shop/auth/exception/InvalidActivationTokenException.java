package com.shop.auth.exception;

/** Thrown when an activation token is not found, already used, or has expired (CS-07). */
public class InvalidActivationTokenException extends RuntimeException {

    /** Constructs the exception. */
    public InvalidActivationTokenException() {
        super("Activation token is invalid or has expired");
    }
}
