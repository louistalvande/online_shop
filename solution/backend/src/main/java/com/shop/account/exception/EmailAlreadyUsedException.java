package com.shop.account.exception;

/** Thrown when an account creation is attempted with an email already registered on the platform. */
public class EmailAlreadyUsedException extends RuntimeException {

    /**
     * Constructs the exception with the conflicting email address.
     *
     * @param email the duplicate email
     */
    public EmailAlreadyUsedException(String email) {
        super("Email already used: " + email);
    }
}
