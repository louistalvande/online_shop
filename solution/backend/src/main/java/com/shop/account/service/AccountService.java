package com.shop.account.service;

import com.shop.account.dto.AccountResponse;
import com.shop.account.dto.CreateAccountRequest;
import com.shop.account.dto.UpdateAccountRequest;
import com.shop.account.exception.AccountNotFoundException;
import com.shop.account.exception.EmailAlreadyUsedException;
import com.shop.account.exception.InvalidAccountStateException;

import java.util.List;
import java.util.UUID;

/** Business operations on platform accounts. */
public interface AccountService {

    /**
     * Creates a new account and sends an activation email (US-ADM-01 / CS-07).
     * Account starts with status {@code PENDING} until the recipient clicks the link.
     *
     * @param request the account creation payload
     * @return the created account
     * @throws EmailAlreadyUsedException if the email is already registered
     */
    AccountResponse createAccount(CreateAccountRequest request);

    /**
     * Returns all accounts on the platform.
     *
     * @return list of all accounts
     */
    List<AccountResponse> listAccounts();

    /**
     * Updates the editable fields of an account (FS-A01 / FS-A02 / CS-10).
     * Only non-null fields in the request are applied.
     *
     * @param id      the account UUID
     * @param request the fields to update
     * @return the updated account
     * @throws AccountNotFoundException if no account exists with the given ID
     */
    AccountResponse updateAccount(UUID id, UpdateAccountRequest request);

    /**
     * Suspends an active account (US-ADM-02 / FS-A01).
     * The account must currently have status {@code ACTIVE}.
     *
     * @param id the account UUID
     * @return the updated account
     * @throws AccountNotFoundException      if no account exists with the given ID
     * @throws InvalidAccountStateException  if the account is not in {@code ACTIVE} status
     */
    AccountResponse suspendAccount(UUID id);

    /**
     * Reactivates a suspended account (US-ADM-03 / FS-A01).
     * The account must currently have status {@code SUSPENDED}.
     *
     * @param id the account UUID
     * @return the updated account
     * @throws AccountNotFoundException      if no account exists with the given ID
     * @throws InvalidAccountStateException  if the account is not in {@code SUSPENDED} status
     */
    AccountResponse reactivateAccount(UUID id);

    /**
     * Force-activates a pending account by setting its status directly to {@code ACTIVE},
     * bypassing the email verification flow. Intended for admin override and test setup.
     *
     * @param id the account UUID
     * @return the updated account
     * @throws AccountNotFoundException     if no account exists with the given ID
     * @throws InvalidAccountStateException if the account is not in {@code PENDING} status
     */
    AccountResponse forceActivateAccount(UUID id);

    /**
     * Soft-deletes an account by setting its status to {@code DELETED}.
     *
     * @param id the account UUID
     * @throws AccountNotFoundException if no account exists with the given ID
     */
    void deleteAccount(UUID id);
}
