package com.shop.auth.controller;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
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

/** Shared authentication endpoint for all actors (admin, vendor, buyer). */
@Tag(name = "Auth", description = "Authentication — shared across all actors")
@RequestMapping("/api/auth")
public interface AuthController {

    @Operation(summary = "Login — returns a signed JWT; requiresPasswordSetup=true on first login")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive account")
    })
    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "Set password — called after first login when requiresPasswordSetup=true")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Password set successfully"),
        @ApiResponse(responseCode = "400", description = "Passwords do not match"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/setup-password")
    ResponseEntity<Void> setupPassword(@Valid @RequestBody SetupPasswordRequest request, Principal principal);
}
