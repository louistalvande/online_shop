package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response body returned on successful authentication or when an MFA challenge is required.
 * The JWT is set as an HttpOnly cookie by the controller (US-SEC-01 / FS-S03);
 * the {@code token} field is kept for API clients and E2E test runners that use Bearer auth.
 */
public class AuthResponse {

    @Schema(description = "Signed JWT — also set as an HttpOnly cookie (US-SEC-01). Browser clients should use the cookie; API clients may use this value as a Bearer token.")
    private final String token;

    @Schema(description = "Authenticated account email")
    private final String email;

    @Schema(description = "Account role — allows browser clients to determine access level without decoding the JWT")
    private final String role;

    @Schema(description = "True when the account has no password yet — client must call /api/auth/setup-password")
    private final boolean requiresPasswordSetup;

    @Schema(description = "True when the account has TOTP MFA enabled — client must call /api/auth/mfa/verify with the mfaToken")
    private final boolean requiresMfa;

    @Schema(description = "Short-lived pre-auth token required by /api/auth/mfa/verify — present only when requiresMfa is true")
    private final String mfaToken;

    /**
     * Constructs a full auth response.
     *
     * @param token                 the signed JWT (also set as cookie by the controller)
     * @param email                 the account email
     * @param role                  the account role string (e.g. "BUYER")
     * @param requiresPasswordSetup whether the client must prompt for password setup
     * @param requiresMfa           whether a second factor is required
     * @param mfaToken              the pre-auth token for the MFA challenge, or {@code null}
     */
    public AuthResponse(String token, String email, String role,
                        boolean requiresPasswordSetup, boolean requiresMfa, String mfaToken) {
        this.token                = token;
        this.email                = email;
        this.role                 = role;
        this.requiresPasswordSetup = requiresPasswordSetup;
        this.requiresMfa          = requiresMfa;
        this.mfaToken             = mfaToken;
    }

    /** Convenience constructor for the common no-MFA case. */
    public AuthResponse(String token, String email, String role, boolean requiresPasswordSetup) {
        this(token, email, role, requiresPasswordSetup, false, null);
    }

    /** @return the JWT token */
    public String getToken() { return token; }

    /** @return the account email */
    public String getEmail() { return email; }

    /** @return the account role */
    public String getRole() { return role; }

    /** @return true if the client must prompt the user to set a password */
    public boolean isRequiresPasswordSetup() { return requiresPasswordSetup; }

    /** @return true if a TOTP second factor must be verified before a JWT is issued */
    public boolean isRequiresMfa() { return requiresMfa; }

    /** @return the pre-auth token for the pending MFA challenge, or {@code null} */
    public String getMfaToken() { return mfaToken; }
}
