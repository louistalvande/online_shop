package com.shop.account.exception;

/** Thrown when a buyer attempts to delete their last remaining active delivery address. */
public class LastActiveAddressException extends RuntimeException {

    /** Creates the exception with a fixed message. */
    public LastActiveAddressException() {
        super("Cannot delete the last active delivery address");
    }
}
