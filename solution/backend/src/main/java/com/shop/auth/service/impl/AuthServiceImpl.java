package com.shop.auth.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountStatus;
import com.shop.account.repository.AccountRepository;
import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.exception.InvalidCredentialsException;
import com.shop.auth.service.AuthService;
import com.shop.common.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Validates credentials against the account store and issues JWT tokens. */
@Service
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Constructs the service with its dependencies.
     *
     * @param accountRepository repository for account lookups
     * @param passwordEncoder   BCrypt encoder for password verification
     * @param jwtUtil           utility for JWT generation
     */
    public AuthServiceImpl(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses a generic error for all failure cases to prevent account enumeration — CS-08.
     * The JWT embeds the account role so each SPA can access its protected endpoints.
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtUtil.generateToken(account.getEmail(), account.getRole().name());
        return new AuthResponse(token, account.getEmail());
    }
}
