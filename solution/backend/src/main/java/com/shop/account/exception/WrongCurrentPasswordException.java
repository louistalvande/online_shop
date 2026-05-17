package com.shop.account.exception;

/** Thrown when the current password provided for a profile update does not match the stored hash. */
public class WrongCurrentPasswordException extends RuntimeException {

    /** Constructs the exception with a default message. */
    public WrongCurrentPasswordException() {
        super("Wrong current password");
    }
}
