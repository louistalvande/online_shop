package com.shop.auth.controller.impl;

import com.shop.auth.controller.AuthController;
import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** REST implementation of the admin authentication controller. */
@RestController
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    /**
     * Constructs the controller with the auth service.
     *
     * @param authService service responsible for credential validation and JWT issuance
     */
    public AuthControllerImpl(AuthService authService) {
        this.authService = authService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        return ResponseEntity.ok(authService.login(request, "ADMIN"));
    }
}
