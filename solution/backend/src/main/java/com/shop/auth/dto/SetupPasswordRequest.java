package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Payload for the first-login password setup endpoint. */
public class SetupPasswordRequest {

    @Schema(description = "Chosen password")
    @NotBlank
    private String password;

    @Schema(description = "Password confirmation — must match password")
    @NotBlank
    private String confirmPassword;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
