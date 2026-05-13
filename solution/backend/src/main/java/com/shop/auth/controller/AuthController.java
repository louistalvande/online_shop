package com.shop.auth.controller;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** Shared authentication endpoint for all actors (admin, vendor, buyer). */
@Tag(name = "Auth", description = "Authentication — shared across all actors")
@RequestMapping("/api/auth")
public interface AuthController {

    /**
     * Authenticates any active account and returns a signed JWT embedding the account role.
     * The caller uses the role to access the appropriate protected endpoints.
     *
     * @param request login credentials (email + password)
     * @return 200 with {@link AuthResponse} on success, 401 if credentials are invalid
     */
    @Operation(summary = "Login — returns a signed JWT with the account role embedded")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive account")
    })
    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);
}
