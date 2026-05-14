package com.shop.auth.controller.impl;

import com.shop.auth.controller.AuthController;
import com.shop.auth.dto.ActivateAccountRequest;
import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.RegisterRequest;
import com.shop.auth.dto.SetupPasswordRequest;
import com.shop.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/** REST implementation of {@link AuthController}. */
@RestController
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    /** @param authService the authentication business logic */
    public AuthControllerImpl(AuthService authService) {
        this.authService = authService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> register(RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(201).build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> activate(ActivateAccountRequest request) {
        authService.activate(request);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> setupPassword(SetupPasswordRequest request, Principal principal) {
        authService.setupPassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
