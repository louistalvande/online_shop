package com.shop.security;

/**
 * Generates and verifies TOTP (RFC 6238) secrets and codes for MFA (SEC-AUTH-007 / CPA-15).
 */
public interface TotpService {

    /**
     * Generates a new Base32-encoded TOTP secret.
     *
     * @return a fresh Base32-encoded secret
     */
    String generateSecret();

    /**
     * Builds an {@code otpauth://} URI suitable for encoding as a QR code in the client UI.
     *
     * @param secret the Base32-encoded TOTP secret
     * @param email  the account email shown as the account name in the authenticator app
     * @return the otpauth URI string
     */
    String buildOtpauthUri(String secret, String email);

    /**
     * Verifies a 6-digit TOTP code against the given secret.
     * A time window of ±1 step (±30 s) is tolerated for clock skew.
     *
     * @param secret the Base32-encoded TOTP secret stored for the account
     * @param code   the 6-digit code submitted by the user
     * @return {@code true} if the code is valid within the accepted time window
     */
    boolean isCodeValid(String secret, String code);
}
