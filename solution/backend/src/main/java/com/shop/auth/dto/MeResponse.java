package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response body for {@code GET /api/auth/me} — session health check. */
public class MeResponse {

    @Schema(description = "Authenticated user's email address")
    private final String email;

    @Schema(description = "Authenticated user's role (BUYER, VENDOR, ADMIN)")
    private final String role;

    /**
     * @param email the principal's email
     * @param role  the principal's role, stripped of the {@code ROLE_} prefix
     */
    public MeResponse(String email, String role) {
        this.email = email;
        this.role  = role;
    }

    /** @return the email */
    public String getEmail() { return email; }

    /** @return the role */
    public String getRole() { return role; }
}
