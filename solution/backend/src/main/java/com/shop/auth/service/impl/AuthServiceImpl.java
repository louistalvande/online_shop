package com.shop.auth.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import com.shop.account.exception.EmailAlreadyUsedException;
import com.shop.account.repository.AccountRepository;
import com.shop.account.repository.ActivationTokenRepository;
import com.shop.account.service.impl.AccountServiceImpl;
import com.shop.auth.dto.ActivateAccountRequest;
import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.RegisterRequest;
import com.shop.auth.dto.SetupPasswordRequest;
import com.shop.auth.exception.InvalidActivationTokenException;
import com.shop.auth.exception.InvalidCredentialsException;
import com.shop.auth.exception.PasswordsMismatchException;
import com.shop.auth.service.AuthService;
import com.shop.common.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Default implementation of {@link AuthService}. */
@Service
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AccountServiceImpl accountServiceImpl;

    /**
     * @param accountRepository         account data access
     * @param activationTokenRepository token data access
     * @param passwordEncoder           BCrypt encoder
     * @param jwtUtil                   JWT generator and validator
     * @param accountServiceImpl        used to issue activation tokens for self-registered buyers
     */
    public AuthServiceImpl(
            AccountRepository accountRepository,
            ActivationTokenRepository activationTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AccountServiceImpl accountServiceImpl) {
        this.accountRepository = accountRepository;
        this.activationTokenRepository = activationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.accountServiceImpl = accountServiceImpl;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException(request.getEmail());
        }

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        account.setRole(AccountRole.BUYER);
        account.setStatus(AccountStatus.PENDING);
        accountRepository.save(account);

        accountServiceImpl.issueActivationToken(account);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void activate(ActivateAccountRequest request) {
        var tokenRecord = activationTokenRepository.findByToken(request.getToken())
                .orElseThrow(InvalidActivationTokenException::new);

        if (tokenRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidActivationTokenException();
        }

        Account account = tokenRecord.getAccount();

        if (account.getPasswordHash() == null) {
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new InvalidCredentialsException();
            }
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new PasswordsMismatchException();
            }
            account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        account.setStatus(AccountStatus.ACTIVE);
        activationTokenRepository.deleteByAccountId(account.getId());
    }

    /** {@inheritDoc} */
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
        return new AuthResponse(token, account.getEmail(), account.isMustChangePassword());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void setupPassword(String email, SetupPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordsMismatchException();
        }

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setMustChangePassword(false);
    }
}
