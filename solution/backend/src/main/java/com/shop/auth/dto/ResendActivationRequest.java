package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Payload for requesting a new activation link (US-REG-03). */
public class ResendActivationRequest {

    @NotBlank
    @Email
    @Schema(description = "Email address of the PENDING account requesting a new activation link")
    private String email;

    /** @return the email address */
    public String getEmail() { return email; }

    /** @param email the email address */
    public void setEmail(String email) { this.email = email; }
}
