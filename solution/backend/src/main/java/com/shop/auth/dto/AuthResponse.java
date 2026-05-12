package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response body returned on successful authentication. */
public class AuthResponse {

    @Schema(description = "Signed JWT to include in subsequent requests as Bearer token")
    private final String token;

    @Schema(description = "Authenticated account email")
    private final String email;

    /**
     * Constructs an authentication response.
     *
     * @param token the signed JWT
     * @param email the authenticated account email
     */
    public AuthResponse(String token, String email) {
        this.token = token;
        this.email = email;
    }

    /** @return the JWT token */
    public String getToken() { return token; }

    /** @return the account email */
    public String getEmail() { return email; }
}
