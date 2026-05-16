package com.shop.account.exception;

import com.shop.account.entity.AccountStatus;

import java.util.UUID;

/** Thrown when a status transition is not allowed for the current account state. */
public class InvalidAccountStateException extends RuntimeException {

    /**
     * Constructs the exception with the account ID and the required status.
     *
     * @param id       the account UUID
     * @param required the status the account must have for the operation to be allowed
     */
    public InvalidAccountStateException(UUID id, AccountStatus required) {
        super("Account " + id + " must have status " + required + " for this operation");
    }
}
