package com.shop.auth.exception;

/**
 * Thrown during login when the account has TOTP MFA enabled and a second-factor
 * verification step is required before issuing a JWT (SEC-AUTH-007 / CPA-15).
 */
public class MfaRequiredException extends RuntimeException {

    /** Short-lived pre-auth token the client must present when calling the MFA verify endpoint. */
    private final String mfaToken;

    /**
     * Constructs the exception.
     *
     * @param mfaToken the pre-auth token identifying the pending MFA challenge
     */
    public MfaRequiredException(String mfaToken) {
        super("MFA verification required");
        this.mfaToken = mfaToken;
    }

    /** @return the pre-auth token for the pending MFA challenge */
    public String getMfaToken() { return mfaToken; }
}
