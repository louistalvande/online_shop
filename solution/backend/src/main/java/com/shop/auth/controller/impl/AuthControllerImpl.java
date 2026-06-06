package com.shop.auth.controller.impl;

import com.shop.auth.controller.AuthController;
import com.shop.auth.dto.*;
import com.shop.auth.service.AuthService;
import com.shop.common.CookieUtil;
import com.shop.common.JwtUtil;
import com.shop.common.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/** REST implementation of {@link AuthController}. */
@RestController
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;

    /**
     * @param authService      the authentication business logic
     * @param cookieUtil       JWT cookie helper
     * @param jwtUtil          JWT parsing utility
     * @param blacklistService token revocation service
     */
    public AuthControllerImpl(AuthService authService, CookieUtil cookieUtil,
                               JwtUtil jwtUtil, TokenBlacklistService blacklistService) {
        this.authService      = authService;
        this.cookieUtil       = cookieUtil;
        this.jwtUtil          = jwtUtil;
        this.blacklistService = blacklistService;
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

    /**
     * {@inheritDoc}
     * Sets the JWT as an HttpOnly cookie (US-SEC-01) in addition to returning it in the body for API clients.
     */
    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest request, HttpServletResponse response) {
        AuthResponse auth = authService.login(request);
        if (auth.getToken() != null) {
            cookieUtil.setJwtCookie(response, auth.getToken());
        }
        return ResponseEntity.ok(auth);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> resendActivation(ResendActivationRequest request) {
        authService.resendActivation(request);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> setupPassword(SetupPasswordRequest request, Principal principal) {
        authService.setupPassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> forgotPassword(ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> resetPassword(ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MfaSetupResponse> initMfaSetup(Principal principal) {
        return ResponseEntity.ok(authService.initMfaSetup(principal.getName()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> confirmMfaSetup(MfaConfirmRequest request, Principal principal) {
        authService.confirmMfaSetup(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     * Sets the JWT as an HttpOnly cookie after successful MFA verification (US-SEC-01).
     */
    @Override
    public ResponseEntity<AuthResponse> verifyMfa(MfaVerifyRequest request, HttpServletResponse response) {
        AuthResponse auth = authService.verifyMfa(request);
        if (auth.getToken() != null) {
            cookieUtil.setJwtCookie(response, auth.getToken());
        }
        return ResponseEntity.ok(auth);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MeResponse> me(Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");
        return ResponseEntity.ok(new MeResponse(principal.getName(), role));
    }

    /**
     * {@inheritDoc}
     * Reads the JWT from the cookie or Authorization header, blacklists it in Redis, and clears the cookie.
     */
    @Override
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = cookieUtil.extractFromCookie(request);
        if (token == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
            }
        }
        if (token != null && jwtUtil.isValid(token)) {
            long remainingMs = jwtUtil.extractExpiration(token).getTime() - System.currentTimeMillis();
            blacklistService.blacklist(token, remainingMs);
        }
        cookieUtil.clearJwtCookie(response);
        return ResponseEntity.noContent().build();
    }
}
