package com.shop.account.exception;

import java.util.UUID;

/** Thrown when an account lookup finds no matching record. */
public class AccountNotFoundException extends RuntimeException {

    /**
     * Constructs the exception with the missing account ID.
     *
     * @param id the account UUID that was not found
     */
    public AccountNotFoundException(UUID id) {
        super("Account not found: " + id);
    }
}
