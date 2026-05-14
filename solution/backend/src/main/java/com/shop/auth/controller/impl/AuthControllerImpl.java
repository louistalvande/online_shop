package com.shop.auth.controller.impl;

import com.shop.auth.controller.AuthController;
import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.SetupPasswordRequest;
import com.shop.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/** REST implementation of the shared authentication controller. */
@RestController
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    public AuthControllerImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Override
    public ResponseEntity<Void> setupPassword(SetupPasswordRequest request, Principal principal) {
        authService.setupPassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
