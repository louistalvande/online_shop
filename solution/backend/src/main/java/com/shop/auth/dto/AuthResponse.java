package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response body returned on successful authentication. */
public class AuthResponse {

    @Schema(description = "Signed JWT to include in subsequent requests as Bearer token")
    private final String token;

    @Schema(description = "Authenticated account email")
    private final String email;

    @Schema(description = "True when the account has no password yet — client must call /api/auth/setup-password")
    private final boolean requiresPasswordSetup;

    public AuthResponse(String token, String email, boolean requiresPasswordSetup) {
        this.token = token;
        this.email = email;
        this.requiresPasswordSetup = requiresPasswordSetup;
    }

    /** @return the JWT token */
    public String getToken() { return token; }

    /** @return the account email */
    public String getEmail() { return email; }

    /** @return true if the client must prompt the user to set a password */
    public boolean isRequiresPasswordSetup() { return requiresPasswordSetup; }
}
