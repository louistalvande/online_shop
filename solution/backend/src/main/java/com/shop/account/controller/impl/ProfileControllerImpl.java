package com.shop.account.controller.impl;

import com.shop.account.controller.ProfileController;
import com.shop.account.dto.ProfileResponse;
import com.shop.account.dto.UpdateProfileRequest;
import com.shop.account.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/** Default implementation of {@link ProfileController}. */
@RestController
public class ProfileControllerImpl implements ProfileController {

    private final AccountService accountService;

    /**
     * @param accountService the account business-logic service
     */
    public ProfileControllerImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProfileResponse> getProfile(Principal principal) {
        return ResponseEntity.ok(accountService.getProfile(principal.getName()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProfileResponse> updateProfile(Principal principal,
                                                          UpdateProfileRequest request) {
        return ResponseEntity.ok(accountService.updateProfile(principal.getName(), request));
    }
}
