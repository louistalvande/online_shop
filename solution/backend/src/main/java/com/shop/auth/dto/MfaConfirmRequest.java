package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Request body for confirming TOTP MFA setup by verifying the first code (SEC-AUTH-007 / CPA-15). */
public class MfaConfirmRequest {

    @Schema(description = "6-digit TOTP code from the authenticator app to confirm the secret was imported correctly")
    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "{auth.mfa.code.format}")
    private String code;

    /** @return the 6-digit TOTP code */
    public String getCode() { return code; }

    /** @param code the TOTP code to set */
    public void setCode(String code) { this.code = code; }
}
