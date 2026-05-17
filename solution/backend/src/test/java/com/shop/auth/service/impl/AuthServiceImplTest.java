package com.shop.auth.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import com.shop.account.repository.AccountRepository;
import com.shop.account.repository.ActivationTokenRepository;
import com.shop.account.service.impl.AccountServiceImpl;
import com.shop.audit.entity.AuditEventType;
import com.shop.audit.service.AuditLogService;
import com.shop.account.entity.ActivationToken;
import com.shop.auth.dto.ActivateAccountRequest;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.ResendActivationRequest;
import com.shop.auth.exception.InvalidActivationTokenException;
import com.shop.auth.exception.InvalidCredentialsException;
import com.shop.auth.exception.TokenNotFoundException;
import com.shop.auth.exception.TooManyLoginAttemptsException;
import com.shop.auth.service.LoginAttemptService;
import com.shop.common.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

/** Unit tests for the security-relevant behaviour of {@link AuthServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock AccountRepository accountRepository;
    @Mock ActivationTokenRepository activationTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock AccountServiceImpl accountServiceImpl;
    @Mock LoginAttemptService loginAttemptService;
    @Mock AuditLogService auditLogService;

    AuthServiceImpl service;

    private static final String EMAIL    = "user@example.com";
    private static final String PASSWORD = "S3cr3t!Pass#01";
    private static final String HASH     = "$2a$12$hash";

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(
                accountRepository,
                activationTokenRepository,
                passwordEncoder,
                jwtUtil,
                accountServiceImpl,
                loginAttemptService,
                auditLogService);
    }

    private LoginRequest loginRequest() {
        LoginRequest req = new LoginRequest();
        req.setEmail(EMAIL);
        req.setPassword(PASSWORD);
        return req;
    }

    private Account activeAccount() {
        Account a = new Account();
        a.setEmail(EMAIL);
        a.setPasswordHash(HASH);
        a.setRole(AccountRole.BUYER);
        a.setStatus(AccountStatus.ACTIVE);
        return a;
    }

    /** login must throw 429 and log ACCOUNT_LOCKED when the account is blocked (SEC-AUTH-003). */
    @Test
    void login_blocked_throwsTooManyAttempts() {
        given(loginAttemptService.isBlocked(EMAIL)).willReturn(true);

        assertThatThrownBy(() -> service.login(loginRequest()))
                .isInstanceOf(TooManyLoginAttemptsException.class);

        then(auditLogService).should().log(eq(AuditEventType.ACCOUNT_LOCKED), eq(EMAIL), any());
        then(accountRepository).shouldHaveNoInteractions();
    }

    /** login must record failure and throw 401 when the account does not exist. */
    @Test
    void login_accountNotFound_recordsFailureAndThrows401() {
        given(loginAttemptService.isBlocked(EMAIL)).willReturn(false);
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(loginRequest()))
                .isInstanceOf(InvalidCredentialsException.class);

        then(loginAttemptService).should().recordFailure(EMAIL);
        then(auditLogService).should().log(eq(AuditEventType.LOGIN_FAILURE), eq(EMAIL), any());
    }

    /** login must record failure and throw 401 when the account is not ACTIVE. */
    @Test
    void login_accountInactive_recordsFailureAndThrows401() {
        Account inactive = activeAccount();
        inactive.setStatus(AccountStatus.SUSPENDED);
        given(loginAttemptService.isBlocked(EMAIL)).willReturn(false);
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.login(loginRequest()))
                .isInstanceOf(InvalidCredentialsException.class);

        then(loginAttemptService).should().recordFailure(EMAIL);
        then(auditLogService).should().log(eq(AuditEventType.LOGIN_FAILURE), eq(EMAIL), any());
    }

    /** login must record failure and throw 401 when the password is wrong. */
    @Test
    void login_wrongPassword_recordsFailureAndThrows401() {
        given(loginAttemptService.isBlocked(EMAIL)).willReturn(false);
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(activeAccount()));
        given(passwordEncoder.matches(PASSWORD, HASH)).willReturn(false);

        assertThatThrownBy(() -> service.login(loginRequest()))
                .isInstanceOf(InvalidCredentialsException.class);

        then(loginAttemptService).should().recordFailure(EMAIL);
        then(auditLogService).should().log(eq(AuditEventType.LOGIN_FAILURE), eq(EMAIL), any());
    }

    /** activate must throw TokenNotFoundException when the token is not found. */
    @Test
    void activate_tokenNotFound_throwsTokenNotFoundException() {
        ActivateAccountRequest req = new ActivateAccountRequest();
        req.setToken("unknown-token");
        given(activationTokenRepository.findByToken("unknown-token")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.activate(req))
                .isInstanceOf(TokenNotFoundException.class);
    }

    /** activate must throw InvalidActivationTokenException when the token is found but expired. */
    @Test
    void activate_tokenExpired_throwsInvalidActivationTokenException() {
        ActivateAccountRequest req = new ActivateAccountRequest();
        req.setToken("expired-token");
        Account account = activeAccount();
        account.setStatus(AccountStatus.PENDING);
        account.setPasswordHash("$2a$12$hash");

        ActivationToken tokenRecord = new ActivationToken();
        tokenRecord.setToken("expired-token");
        tokenRecord.setAccount(account);
        tokenRecord.setExpiresAt(LocalDateTime.now().minusHours(1));

        given(activationTokenRepository.findByToken("expired-token")).willReturn(Optional.of(tokenRecord));

        assertThatThrownBy(() -> service.activate(req))
                .isInstanceOf(InvalidActivationTokenException.class);
    }

    /** resendActivation must call issueActivationToken when account is PENDING. */
    @Test
    void resendActivation_pendingAccount_issuesNewToken() {
        ResendActivationRequest req = new ResendActivationRequest();
        req.setEmail(EMAIL);
        Account account = activeAccount();
        account.setStatus(AccountStatus.PENDING);
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(account));

        service.resendActivation(req);

        then(accountServiceImpl).should().issueActivationToken(account);
        then(auditLogService).should().log(eq(AuditEventType.RESEND_ACTIVATION), eq(EMAIL), any());
    }

    /** resendActivation must do nothing when the email is unknown (prevents enumeration). */
    @Test
    void resendActivation_unknownEmail_doesNothing() {
        ResendActivationRequest req = new ResendActivationRequest();
        req.setEmail("nobody@example.com");
        given(accountRepository.findByEmail("nobody@example.com")).willReturn(Optional.empty());

        service.resendActivation(req);

        then(accountServiceImpl).shouldHaveNoInteractions();
        then(auditLogService).shouldHaveNoInteractions();
    }

    /** resendActivation must do nothing when the account is already ACTIVE (prevents enumeration). */
    @Test
    void resendActivation_activeAccount_doesNothing() {
        ResendActivationRequest req = new ResendActivationRequest();
        req.setEmail(EMAIL);
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(activeAccount()));

        service.resendActivation(req);

        then(accountServiceImpl).shouldHaveNoInteractions();
        then(auditLogService).shouldHaveNoInteractions();
    }

    /** login must clear the counter and log LOGIN_SUCCESS on valid credentials. */
    @Test
    void login_validCredentials_recordsSuccessAndReturnsToken() {
        given(loginAttemptService.isBlocked(EMAIL)).willReturn(false);
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(activeAccount()));
        given(passwordEncoder.matches(PASSWORD, HASH)).willReturn(true);
        given(jwtUtil.generateToken(EMAIL, "BUYER")).willReturn("jwt.token.here");

        service.login(loginRequest());

        then(loginAttemptService).should().recordSuccess(EMAIL);
        then(auditLogService).should().log(eq(AuditEventType.LOGIN_SUCCESS), eq(EMAIL), any());
        then(loginAttemptService).should(never()).recordFailure(any());
    }
}
