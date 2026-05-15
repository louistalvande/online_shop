package com.shop.auth.controller;

import com.shop.auth.dto.ActivateAccountRequest;
import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.RegisterRequest;
import com.shop.auth.dto.SetupPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

/** Shared authentication endpoints for all actors (admin, vendor, buyer). */
@Tag(name = "Auth", description = "Authentication — shared across all actors")
@RequestMapping("/api/auth")
public interface AuthController {

    @Operation(summary = "Buyer self-registration (FS-B01) — sends activation email")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created — activation email sent"),
        @ApiResponse(responseCode = "409", description = "Email already registered"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/register")
    ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request);

    @Operation(summary = "Activate account via email token (US-ADM-01 / US-REG-02)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Account activated"),
        @ApiResponse(responseCode = "410", description = "Token expired or not found"),
        @ApiResponse(responseCode = "400", description = "Password required or mismatch")
    })
    @PostMapping("/activate")
    ResponseEntity<Void> activate(@Valid @RequestBody ActivateAccountRequest request);

    @Operation(summary = "Login — returns a signed JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or account not active")
    })
    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "Set password after first login (when requiresPasswordSetup=true)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Password set"),
        @ApiResponse(responseCode = "400", description = "Passwords do not match"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/setup-password")
    ResponseEntity<Void> setupPassword(@Valid @RequestBody SetupPasswordRequest request, Principal principal);
}
