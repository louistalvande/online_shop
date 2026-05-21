package com.shop.auth.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import com.shop.account.exception.EmailAlreadyUsedException;
import com.shop.account.repository.AccountRepository;
import com.shop.account.repository.ActivationTokenRepository;
import com.shop.account.service.impl.AccountServiceImpl;
import com.shop.audit.entity.AuditEventType;
import com.shop.audit.service.AuditLogService;
import com.shop.auth.dto.*;
import com.shop.auth.entity.PasswordResetToken;
import com.shop.auth.exception.*;
import com.shop.auth.repository.PasswordResetTokenRepository;
import com.shop.auth.service.AuthService;
import com.shop.auth.service.LoginAttemptService;
import com.shop.common.JwtUtil;
import com.shop.notification.service.NotificationService;
import com.shop.security.PasswordBreachService;
import com.shop.security.TotpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Default implementation of {@link AuthService}. */
@Service
public class AuthServiceImpl implements AuthService {

    /** Redis key prefix for pre-auth MFA tokens. TTL: 5 minutes. */
    private static final String MFA_PENDING_KEY = "mfa:pending:";
    private static final long MFA_TOKEN_TTL_SECONDS = 300;

    /** Password-reset token TTL: 1 hour (CPA-17). */
    private static final long RESET_TOKEN_TTL_HOURS = 1;

    private final AccountRepository accountRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AccountServiceImpl accountServiceImpl;
    private final LoginAttemptService loginAttemptService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final PasswordBreachService passwordBreachService;
    private final TotpService totpService;
    private final StringRedisTemplate redisTemplate;
    private final String passwordResetBaseUrl;

    /**
     * @param accountRepository            account data access
     * @param activationTokenRepository    activation token data access
     * @param passwordResetTokenRepository password reset token data access
     * @param passwordEncoder              BCrypt encoder
     * @param jwtUtil                      JWT generator and validator
     * @param accountServiceImpl           used to issue activation tokens
     * @param loginAttemptService          brute-force protection (SEC-AUTH-003)
     * @param auditLogService              security event audit trail (SEC-LOG-001)
     * @param notificationService          email delivery
     * @param passwordBreachService        HIBP k-anonymity check (SEC-PWD-002)
     * @param totpService                  TOTP secret generation and verification (SEC-AUTH-007)
     * @param redisTemplate                Redis client for MFA pre-auth tokens
     * @param passwordResetBaseUrl         frontend base URL for password-reset links
     */
    public AuthServiceImpl(
            AccountRepository accountRepository,
            ActivationTokenRepository activationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AccountServiceImpl accountServiceImpl,
            LoginAttemptService loginAttemptService,
            AuditLogService auditLogService,
            NotificationService notificationService,
            PasswordBreachService passwordBreachService,
            TotpService totpService,
            StringRedisTemplate redisTemplate,
            @Value("${app.password-reset-base-url}") String passwordResetBaseUrl) {
        this.accountRepository = accountRepository;
        this.activationTokenRepository = activationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.accountServiceImpl = accountServiceImpl;
        this.loginAttemptService = loginAttemptService;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.passwordBreachService = passwordBreachService;
        this.totpService = totpService;
        this.redisTemplate = redisTemplate;
        this.passwordResetBaseUrl = passwordResetBaseUrl;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException(request.getEmail());
        }

        if (passwordBreachService.isCompromised(request.getPassword())) {
            throw new PasswordCompromisedException();
        }

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        account.setRole(AccountRole.BUYER);
        account.setStatus(AccountStatus.PENDING);
        // BUYER accounts have no password expiry (SEC-PWD-003 / CPA-17)
        accountRepository.save(account);

        accountServiceImpl.issueActivationToken(account);
        auditLogService.log(AuditEventType.REGISTRATION, request.getEmail(), null);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void activate(ActivateAccountRequest request) {
        var tokenRecord = activationTokenRepository.findByToken(request.getToken())
                .orElseThrow(TokenNotFoundException::new);

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
            if (passwordBreachService.isCompromised(request.getPassword())) {
                throw new PasswordCompromisedException();
            }
            account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            setPasswordExpiryByRole(account);
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

        // Password revocation check (SEC-PWD-005 / CPA-17)
        if (account.isPasswordRevoked()) {
            loginAttemptService.recordSuccess(email);
            String token = jwtUtil.generateToken(account.getEmail(), account.getRole().name());
            auditLogService.log(AuditEventType.LOGIN_SUCCESS, email, "Revoked password — must change");
            return new AuthResponse(token, account.getEmail(), true);
        }

        // Password expiry check (SEC-PWD-003/004 / CPA-17)
        if (account.getPasswordExpiresAt() != null
                && OffsetDateTime.now(ZoneOffset.UTC).isAfter(account.getPasswordExpiresAt())) {
            loginAttemptService.recordSuccess(email);
            String token = jwtUtil.generateToken(account.getEmail(), account.getRole().name());
            auditLogService.log(AuditEventType.LOGIN_SUCCESS, email, "Expired password — must change");
            return new AuthResponse(token, account.getEmail(), true);
        }

        loginAttemptService.recordSuccess(email);

        // MFA challenge (SEC-AUTH-007 / CPA-15)
        if (account.isTotpEnabled()) {
            String mfaToken = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(
                    MFA_PENDING_KEY + mfaToken,
                    account.getEmail(),
                    MFA_TOKEN_TTL_SECONDS,
                    TimeUnit.SECONDS);
            auditLogService.log(AuditEventType.LOGIN_SUCCESS, email, "Password OK — MFA required");
            return new AuthResponse(null, account.getEmail(), false, true, mfaToken);
        }

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

        if (passwordBreachService.isCompromised(request.getPassword())) {
            throw new PasswordCompromisedException();
        }

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setMustChangePassword(false);
        account.setPasswordRevoked(false);
        setPasswordExpiryByRole(account);
        auditLogService.log(AuditEventType.PASSWORD_CHANGED, email, "First-login password setup");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void resendActivation(ResendActivationRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail()).orElse(null);
        if (account == null || account.getStatus() != AccountStatus.PENDING) {
            return;
        }
        accountServiceImpl.issueActivationToken(account);
        auditLogService.log(AuditEventType.RESEND_ACTIVATION, account.getEmail(), null);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail()).orElse(null);
        // Silently return if unknown — never reveal whether the address exists
        if (account == null || account.getStatus() != AccountStatus.ACTIVE) {
            return;
        }

        // Invalidate any existing token for this account
        passwordResetTokenRepository.deleteByAccountId(account.getId());

        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(tokenValue);
        resetToken.setAccount(account);
        resetToken.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusHours(RESET_TOKEN_TTL_HOURS));
        passwordResetTokenRepository.save(resetToken);

        String resetLink = passwordResetBaseUrl + "/reset-password?token=" + tokenValue;
        Locale locale = resolveLocale(account);
        notificationService.sendPasswordResetEmail(account.getEmail(), resetLink, locale);
        auditLogService.log(AuditEventType.PASSWORD_RESET_REQUESTED, account.getEmail(), null);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(InvalidResetTokenException::new);

        if (resetToken.isUsed()
                || OffsetDateTime.now(ZoneOffset.UTC).isAfter(resetToken.getExpiresAt())) {
            throw new InvalidResetTokenException();
        }

        if (passwordBreachService.isCompromised(request.getNewPassword())) {
            throw new PasswordCompromisedException();
        }

        Account account = resetToken.getAccount();
        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        account.setPasswordRevoked(false);
        account.setMustChangePassword(false);
        setPasswordExpiryByRole(account);

        resetToken.setUsed(true);
        auditLogService.log(AuditEventType.PASSWORD_RESET, account.getEmail(), null);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MfaSetupResponse initMfaSetup(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        String secret = totpService.generateSecret();
        account.setTotpSecret(secret);
        // totpEnabled remains false until confirmMfaSetup succeeds

        String otpauthUri = totpService.buildOtpauthUri(secret, email);
        return new MfaSetupResponse(secret, otpauthUri);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void confirmMfaSetup(String email, MfaConfirmRequest request) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (account.getTotpSecret() == null || !totpService.isCodeValid(account.getTotpSecret(), request.getCode())) {
            throw new InvalidMfaCodeException();
        }

        account.setTotpEnabled(true);
        auditLogService.log(AuditEventType.MFA_ENABLED, email, null);
    }

    /** {@inheritDoc} */
    @Override
    public AuthResponse verifyMfa(MfaVerifyRequest request) {
        String redisKey = MFA_PENDING_KEY + request.getMfaToken();
        String email = redisTemplate.opsForValue().get(redisKey);

        if (email == null) {
            auditLogService.log(AuditEventType.MFA_LOGIN_FAILURE, null, "MFA token missing or expired");
            throw new InvalidMfaCodeException();
        }

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(InvalidMfaCodeException::new);

        if (!totpService.isCodeValid(account.getTotpSecret(), request.getCode())) {
            auditLogService.log(AuditEventType.MFA_LOGIN_FAILURE, email, "Invalid TOTP code");
            throw new InvalidMfaCodeException();
        }

        redisTemplate.delete(redisKey);
        auditLogService.log(AuditEventType.MFA_LOGIN_SUCCESS, email, null);
        String token = jwtUtil.generateToken(account.getEmail(), account.getRole().name());
        return new AuthResponse(token, account.getEmail(), false);
    }

    /**
     * Sets password expiry based on the account role: BUYER → no expiry; ADMIN/VENDOR → 2 years (CPA-17).
     *
     * @param account the account to update
     */
    private void setPasswordExpiryByRole(Account account) {
        if (account.getRole() == AccountRole.BUYER) {
            account.setPasswordExpiresAt(null);
        } else {
            account.setPasswordExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusYears(2));
        }
    }

    /**
     * Resolves the locale from the account's preferred language.
     *
     * @param account the account
     * @return the corresponding {@link Locale}
     */
    private Locale resolveLocale(Account account) {
        return switch (account.getLanguage()) {
            case EN -> Locale.ENGLISH;
            default -> Locale.FRENCH;
        };
    }
}
