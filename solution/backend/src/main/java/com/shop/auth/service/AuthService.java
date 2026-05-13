package com.shop.auth.service;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.exception.InvalidCredentialsException;

/** Authenticates actors and issues JWT tokens embedding their role. */
public interface AuthService {

    /**
     * Validates the credentials and returns a JWT for any active account.
     *
     * @param request login payload containing email and password
     * @return an {@link AuthResponse} containing the signed JWT and the account email
     * @throws InvalidCredentialsException if the email is unknown, the password is wrong,
     *                                     or the account is not active
     */
    AuthResponse login(LoginRequest request);
}
