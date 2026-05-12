package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Payload for the admin login endpoint. */
public class LoginRequest {

    @Schema(description = "Admin account email address")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "Account password")
    @NotBlank
    private String password;

    /** @return the email address */
    public String getEmail() { return email; }

    /** @param email the email address */
    public void setEmail(String email) { this.email = email; }

    /** @return the password */
    public String getPassword() { return password; }

    /** @param password the password */
    public void setPassword(String password) { this.password = password; }
}
