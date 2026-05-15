package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload for account activation (US-ADM-01 / US-REG-02).
 * For admin-created accounts (no password yet): password and confirmPassword are required.
 * For self-registered buyers (password already set): only token is required.
 */
public class ActivateAccountRequest {

    /** Activation token from the email link. */
    @Schema(description = "Activation token received by email")
    @NotBlank
    private String token;

    /** Chosen password — required only for admin-created accounts that have no password yet. */
    @Schema(description = "Chosen password (required when account has no password yet)")
    @Size(min = 8)
    private String password;

    /** Password confirmation — must match password when provided. */
    @Schema(description = "Password confirmation")
    private String confirmPassword;

    /** @return the token */
    public String getToken() { return token; }

    /** @param token the token to set */
    public void setToken(String token) { this.token = token; }

    /** @return the password, or null if not provided */
    public String getPassword() { return password; }

    /** @param password the password to set */
    public void setPassword(String password) { this.password = password; }

    /** @return the confirmation password, or null if not provided */
    public String getConfirmPassword() { return confirmPassword; }

    /** @param confirmPassword the confirmation to set */
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
