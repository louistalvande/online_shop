package com.shop.auth.service;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.exception.InvalidCredentialsException;

/** Authenticates internal actors and issues JWT tokens. */
public interface AuthService {

    /**
     * Validates the credentials and returns a JWT if they are correct.
     *
     * @param request login payload containing email and password
     * @param requiredRole the role the account must hold (e.g. "ADMIN")
     * @return an {@link AuthResponse} containing the signed JWT and the account email
     * @throws InvalidCredentialsException if the email is unknown, the password is wrong,
     *                                     the account does not hold the required role,
     *                                     or the account is not active
     */
    AuthResponse login(LoginRequest request, String requiredRole);
}
