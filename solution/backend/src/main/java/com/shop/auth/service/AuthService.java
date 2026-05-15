package com.shop.auth.service;

import com.shop.account.exception.EmailAlreadyUsedException;
import com.shop.auth.dto.ActivateAccountRequest;
import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.RegisterRequest;
import com.shop.auth.dto.SetupPasswordRequest;
import com.shop.auth.exception.InvalidActivationTokenException;
import com.shop.auth.exception.InvalidCredentialsException;
import com.shop.auth.exception.PasswordsMismatchException;

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
     * For admin-created accounts (no password yet): password and confirmPassword are required.
     * For self-registered buyers (password already set): only token is required.
     *
     * @param request activation payload
     * @throws InvalidActivationTokenException if the token is unknown or expired
     * @throws PasswordsMismatchException      if password and confirmPassword differ
     * @throws InvalidCredentialsException     if password is missing for an account that needs one
     */
    void activate(ActivateAccountRequest request);

    /**
     * Validates credentials and returns a JWT.
     * If the account has no password yet, returns {@code requiresPasswordSetup = true}.
     *
     * @param request login payload
     * @return signed JWT and setup flag
     * @throws InvalidCredentialsException if credentials are invalid or account is not active
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
}
