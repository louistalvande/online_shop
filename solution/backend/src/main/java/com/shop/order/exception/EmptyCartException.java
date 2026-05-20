package com.shop.order.exception;

/** Thrown when a buyer attempts to check out with an empty cart. */
public class EmptyCartException extends RuntimeException {

    /** Constructs the exception with a fixed message. */
    public EmptyCartException() {
        super("Cannot check out with an empty cart");
    }
}
