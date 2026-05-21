package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response body returned on successful authentication or when an MFA challenge is required. */
public class AuthResponse {

    @Schema(description = "Signed JWT to include in subsequent requests as Bearer token — null when requiresMfa is true")
    private final String token;

    @Schema(description = "Authenticated account email")
    private final String email;

    @Schema(description = "True when the account has no password yet — client must call /api/auth/setup-password")
    private final boolean requiresPasswordSetup;

    @Schema(description = "True when the account has TOTP MFA enabled — client must call /api/auth/mfa/verify with the mfaToken")
    private final boolean requiresMfa;

    @Schema(description = "Short-lived pre-auth token required by /api/auth/mfa/verify — present only when requiresMfa is true")
    private final String mfaToken;

    /**
     * Constructs a full auth response.
     *
     * @param token                the signed JWT, or {@code null} when MFA is pending
     * @param email                the account email
     * @param requiresPasswordSetup whether the client must prompt for password setup
     * @param requiresMfa          whether a second factor is required
     * @param mfaToken             the pre-auth token for the MFA challenge, or {@code null}
     */
    public AuthResponse(String token, String email, boolean requiresPasswordSetup,
                        boolean requiresMfa, String mfaToken) {
        this.token = token;
        this.email = email;
        this.requiresPasswordSetup = requiresPasswordSetup;
        this.requiresMfa = requiresMfa;
        this.mfaToken = mfaToken;
    }

    /** Convenience constructor for the common case where no MFA challenge is needed. */
    public AuthResponse(String token, String email, boolean requiresPasswordSetup) {
        this(token, email, requiresPasswordSetup, false, null);
    }

    /** @return the JWT token, or {@code null} when MFA verification is pending */
    public String getToken() { return token; }

    /** @return the account email */
    public String getEmail() { return email; }

    /** @return true if the client must prompt the user to set a password */
    public boolean isRequiresPasswordSetup() { return requiresPasswordSetup; }

    /** @return true if a TOTP second factor must be verified before a JWT is issued */
    public boolean isRequiresMfa() { return requiresMfa; }

    /** @return the pre-auth token for the pending MFA challenge, or {@code null} */
    public String getMfaToken() { return mfaToken; }
}
