package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Payload for the first-login password setup endpoint. */
public class SetupPasswordRequest {

    @Schema(description = "Chosen password — minimum 12 characters, must contain uppercase, lowercase, digit and special character")
    @NotBlank
    @Size(min = 12, message = "{error.password.size}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-#]).{12,}$",
             message = "{error.password.complexity}")
    private String password;

    @Schema(description = "Password confirmation — must match password")
    @NotBlank
    private String confirmPassword;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
