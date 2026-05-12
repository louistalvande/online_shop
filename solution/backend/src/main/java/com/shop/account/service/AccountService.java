package com.shop.account.service;

import com.shop.account.dto.AccountResponse;
import com.shop.account.dto.CreateAccountRequest;
import com.shop.account.exception.EmailAlreadyUsedException;

import java.util.List;

/** Business operations on platform accounts. */
public interface AccountService {

    /**
     * Creates a new account from the given request.
     * The account is immediately set to {@code ACTIVE} status.
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
}
