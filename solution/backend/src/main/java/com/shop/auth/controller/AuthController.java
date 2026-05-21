package com.shop.auth.controller;

import com.shop.auth.dto.*;
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
        @ApiResponse(responseCode = "422", description = "Password found in known data breaches"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/register")
    ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request);

    @Operation(summary = "Activate account via email token (US-ADM-01 / US-REG-02)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Account activated"),
        @ApiResponse(responseCode = "410", description = "Token expired or not found"),
        @ApiResponse(responseCode = "422", description = "Password found in known data breaches"),
        @ApiResponse(responseCode = "400", description = "Password required or mismatch")
    })
    @PostMapping("/activate")
    ResponseEntity<Void> activate(@Valid @RequestBody ActivateAccountRequest request);

    @Operation(summary = "Login — returns a signed JWT or an MFA challenge")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Credentials valid — JWT issued or MFA required"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or account not active"),
        @ApiResponse(responseCode = "429", description = "Too many failed attempts")
    })
    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "Resend activation link to a PENDING account (US-REG-03)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Request processed — email sent if account is PENDING"),
        @ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    @PostMapping("/resend-activation")
    ResponseEntity<Void> resendActivation(@Valid @RequestBody ResendActivationRequest request);

    @Operation(summary = "Set password after first login (when requiresPasswordSetup=true)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Password set"),
        @ApiResponse(responseCode = "422", description = "Password found in known data breaches"),
        @ApiResponse(responseCode = "400", description = "Passwords do not match"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/setup-password")
    ResponseEntity<Void> setupPassword(@Valid @RequestBody SetupPasswordRequest request, Principal principal);

    @Operation(summary = "Request a password-reset link by email (SEC-PWD-006)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Request processed — email sent if account exists"),
        @ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    @PostMapping("/forgot-password")
    ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request);

    @Operation(summary = "Reset password using the one-time token received by email (SEC-PWD-006)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Password updated"),
        @ApiResponse(responseCode = "410", description = "Token expired, not found, or already used"),
        @ApiResponse(responseCode = "422", description = "Password found in known data breaches"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/reset-password")
    ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request);

    @Operation(summary = "Initialise TOTP MFA setup — returns secret and otpauth URI (SEC-AUTH-007)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Secret generated — display QR code to the user"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/mfa/setup/init")
    ResponseEntity<MfaSetupResponse> initMfaSetup(Principal principal);

    @Operation(summary = "Confirm TOTP MFA setup by verifying the first code (SEC-AUTH-007)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "MFA activated"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired TOTP code")
    })
    @PostMapping("/mfa/setup/confirm")
    ResponseEntity<Void> confirmMfaSetup(@Valid @RequestBody MfaConfirmRequest request, Principal principal);

    @Operation(summary = "Verify TOTP code after password login — issues the final JWT (SEC-AUTH-007)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "MFA verified — JWT issued"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired MFA token or code")
    })
    @PostMapping("/mfa/verify")
    ResponseEntity<AuthResponse> verifyMfa(@Valid @RequestBody MfaVerifyRequest request);
}
