package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request body for resetting a password using a one-time token (SEC-PWD-006 / CPA-17). */
public class ResetPasswordRequest {

    @Schema(description = "One-time reset token received by email")
    @NotBlank
    private String token;

    @Schema(description = "New password — min 12 chars, must contain uppercase, lowercase, digit, and special character")
    @NotBlank
    @Size(min = 12)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$",
             message = "{auth.password.complexity}")
    private String newPassword;

    /** @return the one-time reset token */
    public String getToken() { return token; }

    /** @param token the reset token to set */
    public void setToken(String token) { this.token = token; }

    /** @return the new plaintext password */
    public String getNewPassword() { return newPassword; }

    /** @param newPassword the new password to set */
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
