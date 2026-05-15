package com.shop.account.controller.impl;

import com.shop.account.controller.AccountController;
import com.shop.account.dto.AccountResponse;
import com.shop.account.dto.CreateAccountRequest;
import com.shop.account.dto.UpdateAccountRequest;
import com.shop.account.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/** {@link AccountController} implementation. */
@RestController
public class AccountControllerImpl implements AccountController {

    private final AccountService accountService;

    /**
     * Constructs the controller with its service dependency.
     *
     * @param accountService the account business logic layer
     */
    public AccountControllerImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AccountResponse> createAccount(CreateAccountRequest request) {
        AccountResponse created = accountService.createAccount(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<AccountResponse>> listAccounts() {
        return ResponseEntity.ok(accountService.listAccounts());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AccountResponse> updateAccount(UUID id, UpdateAccountRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(id, request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AccountResponse> suspendAccount(UUID id) {
        return ResponseEntity.ok(accountService.suspendAccount(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AccountResponse> reactivateAccount(UUID id) {
        return ResponseEntity.ok(accountService.reactivateAccount(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> deleteAccount(UUID id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
