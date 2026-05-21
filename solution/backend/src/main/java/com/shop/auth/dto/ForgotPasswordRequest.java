package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request body for the forgot-password flow (SEC-PWD-006 / CPA-17). */
public class ForgotPasswordRequest {

    @Schema(description = "Email address of the account to reset the password for")
    @NotBlank
    @Email
    private String email;

    /** @return the email address */
    public String getEmail() { return email; }

    /** @param email the email address to set */
    public void setEmail(String email) { this.email = email; }
}
