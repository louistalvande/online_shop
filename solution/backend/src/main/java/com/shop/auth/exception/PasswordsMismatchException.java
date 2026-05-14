package com.shop.auth.exception;

/** Thrown when password and confirmPassword do not match during setup. */
public class PasswordsMismatchException extends RuntimeException {
    public PasswordsMismatchException() {
        super("Passwords do not match");
    }
}
