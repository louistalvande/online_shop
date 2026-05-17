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
import com.shop.audit.entity.AuditEventType;
import com.shop.audit.service.AuditLogService;
import com.shop.auth.exception.InvalidActivationTokenException;
import com.shop.auth.exception.InvalidCredentialsException;
import com.shop.auth.exception.PasswordsMismatchException;
import com.shop.auth.exception.TooManyLoginAttemptsException;
import com.shop.auth.service.AuthService;
import com.shop.auth.service.LoginAttemptService;
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
    private final LoginAttemptService loginAttemptService;
    private final AuditLogService auditLogService;

    /**
     * @param accountRepository         account data access
     * @param activationTokenRepository token data access
     * @param passwordEncoder           BCrypt encoder
     * @param jwtUtil                   JWT generator and validator
     * @param accountServiceImpl        used to issue activation tokens for self-registered buyers
     * @param loginAttemptService       brute-force protection (SEC-AUTH-003)
     * @param auditLogService           security event audit trail (SEC-LOG-001)
     */
    public AuthServiceImpl(
            AccountRepository accountRepository,
            ActivationTokenRepository activationTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AccountServiceImpl accountServiceImpl,
            LoginAttemptService loginAttemptService,
            AuditLogService auditLogService) {
        this.accountRepository = accountRepository;
        this.activationTokenRepository = activationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.accountServiceImpl = accountServiceImpl;
        this.loginAttemptService = loginAttemptService;
        this.auditLogService = auditLogService;
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
        auditLogService.log(AuditEventType.REGISTRATION, request.getEmail(), null);
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
        auditLogService.log(AuditEventType.ACCOUNT_ACTIVATED, account.getEmail(), null);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();

        if (loginAttemptService.isBlocked(email)) {
            auditLogService.log(AuditEventType.ACCOUNT_LOCKED, email, "Login blocked — too many failed attempts");
            throw new TooManyLoginAttemptsException(email);
        }

        Account account = accountRepository.findByEmail(email).orElse(null);

        if (account == null || account.getStatus() != AccountStatus.ACTIVE) {
            loginAttemptService.recordFailure(email);
            auditLogService.log(AuditEventType.LOGIN_FAILURE, email, "Account not found or inactive");
            throw new InvalidCredentialsException();
        }

        if (account.getPasswordHash() == null) {
            loginAttemptService.recordSuccess(email);
            String token = jwtUtil.generateToken(account.getEmail(), account.getRole().name());
            auditLogService.log(AuditEventType.LOGIN_SUCCESS, email, "First login — no password set yet");
            return new AuthResponse(token, account.getEmail(), true);
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            loginAttemptService.recordFailure(email);
            auditLogService.log(AuditEventType.LOGIN_FAILURE, email, "Bad password");
            throw new InvalidCredentialsException();
        }

        loginAttemptService.recordSuccess(email);
        auditLogService.log(AuditEventType.LOGIN_SUCCESS, email, null);
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
        auditLogService.log(AuditEventType.PASSWORD_CHANGED, email, "First-login password setup");
    }
}
