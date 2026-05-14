package com.shop.auth.service;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.SetupPasswordRequest;
import com.shop.auth.exception.InvalidCredentialsException;
import com.shop.auth.exception.PasswordsMismatchException;

/** Authenticates actors and issues JWT tokens embedding their role. */
public interface AuthService {

    /**
     * Validates the credentials and returns a JWT for any active account.
     * If the account has no password yet, skips the password check and returns
     * {@code requiresPasswordSetup = true} so the client can redirect to setup.
     *
     * @param request login payload containing email and optional password
     * @return an {@link AuthResponse} containing the signed JWT and setup flag
     * @throws InvalidCredentialsException if the email is unknown, the password is wrong,
     *                                     or the account is not active
     */
    AuthResponse login(LoginRequest request);

    /**
     * Sets the password for an account that has not yet configured one.
     *
     * @param email   the authenticated account email (from JWT)
     * @param request the new password and its confirmation
     * @throws PasswordsMismatchException  if password and confirmPassword differ
     * @throws InvalidCredentialsException if the account is not found
     */
    void setupPassword(String email, SetupPasswordRequest request);
}
