package com.shop.auth.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountStatus;
import com.shop.account.repository.AccountRepository;
import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.SetupPasswordRequest;
import com.shop.auth.exception.InvalidCredentialsException;
import com.shop.auth.exception.PasswordsMismatchException;
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

    public AuthServiceImpl(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }

        if (account.getPasswordHash() == null) {
            String token = jwtUtil.generateToken(account.getEmail(), account.getRole().name());
            return new AuthResponse(token, account.getEmail(), true);
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtUtil.generateToken(account.getEmail(), account.getRole().name());
        return new AuthResponse(token, account.getEmail(), false);
    }

    @Override
    @Transactional
    public void setupPassword(String email, SetupPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordsMismatchException();
        }

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    }
}
