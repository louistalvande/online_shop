package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response returned when initiating TOTP MFA setup (SEC-AUTH-007 / CPA-15). */
public class MfaSetupResponse {

    @Schema(description = "Base32-encoded TOTP secret to import into the authenticator app")
    private final String secret;

    @Schema(description = "otpauth:// URI to encode as a QR code in the client UI")
    private final String otpauthUri;

    /**
     * Constructs the response.
     *
     * @param secret     the Base32-encoded TOTP secret
     * @param otpauthUri the otpauth URI for QR code generation
     */
    public MfaSetupResponse(String secret, String otpauthUri) {
        this.secret = secret;
        this.otpauthUri = otpauthUri;
    }

    /** @return the Base32-encoded TOTP secret */
    public String getSecret() { return secret; }

    /** @return the otpauth URI */
    public String getOtpauthUri() { return otpauthUri; }
}
