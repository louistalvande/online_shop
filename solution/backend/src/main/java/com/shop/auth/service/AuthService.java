package com.shop.auth.service;

import com.shop.account.exception.EmailAlreadyUsedException;
import com.shop.auth.dto.*;
import com.shop.auth.exception.*;

/** Authenticates actors and issues JWT tokens. */
public interface AuthService {

    /**
     * Registers a new buyer account and sends an activation email (US-REG-01 / FS-B01 / CS-07).
     * Account starts PENDING until the buyer clicks the activation link.
     *
     * @param request registration payload
     * @throws EmailAlreadyUsedException if the email is already registered
     */
    void register(RegisterRequest request);

    /**
     * Activates an account using the token received by email (US-ADM-01 / US-REG-02 / CS-07).
     * Performs a HIBP k-anonymity check on the new password (SEC-PWD-002 / CPA-16).
     * Sets password_expires_at by role (SEC-PWD-003/004 / CPA-17).
     *
     * @param request activation payload
     * @throws InvalidActivationTokenException if the token is unknown or expired
     * @throws PasswordsMismatchException      if password and confirmPassword differ
     * @throws InvalidCredentialsException     if password is missing for an account that needs one
     * @throws PasswordCompromisedException    if the chosen password is in the HIBP database
     */
    void activate(ActivateAccountRequest request);

    /**
     * Validates credentials and returns a JWT.
     * If the account has no password yet, returns {@code requiresPasswordSetup = true}.
     * If the account has TOTP MFA enabled, stores a short-lived pre-auth token in Redis
     * and returns {@code requiresMfa = true} with the {@code mfaToken}.
     *
     * @param request login payload
     * @return signed JWT and setup flags
     * @throws InvalidCredentialsException  if credentials are invalid or account is not active
     * @throws TooManyLoginAttemptsException if the account is temporarily locked
     */
    AuthResponse login(LoginRequest request);

    /**
     * Sets the password for an account that has not yet configured one.
     *
     * @param email   authenticated account email (from JWT)
     * @param request new password and confirmation
     * @throws PasswordsMismatchException  if password and confirmPassword differ
     * @throws InvalidCredentialsException if the account is not found
     */
    void setupPassword(String email, SetupPasswordRequest request);

    /**
     * Resends an activation link to a PENDING account (US-REG-03 / CS-07).
     * Always returns silently to prevent email enumeration.
     *
     * @param request payload containing the email address
     */
    void resendActivation(ResendActivationRequest request);

    /**
     * Issues a one-time password-reset token and sends it by email (SEC-PWD-006 / CPA-17).
     * Always returns silently — never reveals whether the address exists.
     *
     * @param request payload containing the email address
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Validates the reset token, checks the new password against HIBP, and updates the hash
     * (SEC-PWD-006 / CPA-17). Marks the token as used.
     *
     * @param request payload with token and new password
     * @throws InvalidResetTokenException   if the token is unknown, expired, or already used
     * @throws PasswordCompromisedException if the new password is in the HIBP database
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Generates a new TOTP secret for the authenticated user and returns it with an otpauth URI
     * for QR code display (SEC-AUTH-007 / CPA-15).
     * The secret is stored (unconfirmed) until {@link #confirmMfaSetup} is called.
     *
     * @param email the authenticated account email
     * @return the TOTP secret and otpauth URI
     * @throws InvalidCredentialsException if the account is not found
     */
    MfaSetupResponse initMfaSetup(String email);

    /**
     * Verifies the first TOTP code to confirm the user imported the secret correctly,
     * then marks TOTP as enabled for the account (SEC-AUTH-007 / CPA-15).
     *
     * @param email   the authenticated account email
     * @param request the TOTP code to verify
     * @throws InvalidCredentialsException if the account is not found
     * @throws InvalidMfaCodeException     if the code is invalid or the secret is not yet initialised
     */
    void confirmMfaSetup(String email, MfaConfirmRequest request);

    /**
     * Verifies the TOTP code submitted after the first authentication step and issues a JWT
     * (SEC-AUTH-007 / CPA-15).
     *
     * @param request the pre-auth token and 6-digit TOTP code
     * @return the signed JWT
     * @throws InvalidMfaCodeException if the pre-auth token is missing/expired or the code is wrong
     */
    AuthResponse verifyMfa(MfaVerifyRequest request);
}
