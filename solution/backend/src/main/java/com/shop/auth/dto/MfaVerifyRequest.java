package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Request body for verifying a TOTP code during login or MFA setup confirmation (SEC-AUTH-007 / CPA-15). */
public class MfaVerifyRequest {

    @Schema(description = "Pre-auth token returned by the login endpoint when requiresMfa is true")
    @NotBlank
    private String mfaToken;

    @Schema(description = "6-digit TOTP code from the authenticator app")
    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "{auth.mfa.code.format}")
    private String code;

    /** @return the pre-auth token */
    public String getMfaToken() { return mfaToken; }

    /** @param mfaToken the pre-auth token to set */
    public void setMfaToken(String mfaToken) { this.mfaToken = mfaToken; }

    /** @return the 6-digit TOTP code */
    public String getCode() { return code; }

    /** @param code the TOTP code to set */
    public void setCode(String code) { this.code = code; }
}
