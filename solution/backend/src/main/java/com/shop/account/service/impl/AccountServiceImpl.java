package com.shop.account.service.impl;

import com.shop.account.dto.AccountResponse;
import com.shop.account.dto.CreateAccountRequest;
import com.shop.account.entity.Account;
import com.shop.account.entity.AccountStatus;
import com.shop.account.entity.ActivationToken;
import com.shop.account.exception.EmailAlreadyUsedException;
import com.shop.account.repository.AccountRepository;
import com.shop.account.repository.ActivationTokenRepository;
import com.shop.account.service.AccountService;
import com.shop.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Default implementation of {@link AccountService}. */
@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final NotificationService notificationService;
    private final String activationBaseUrl;
    private final int activationExpiryHours;

    /**
     * @param accountRepository         account data access
     * @param activationTokenRepository token data access
     * @param notificationService       email sender
     * @param activationBaseUrl         base URL for the activation link
     * @param activationExpiryHours     token TTL in hours (CS-07)
     */
    public AccountServiceImpl(
            AccountRepository accountRepository,
            ActivationTokenRepository activationTokenRepository,
            NotificationService notificationService,
            @Value("${app.activation-base-url}") String activationBaseUrl,
            @Value("${app.activation-expiry-hours}") int activationExpiryHours) {
        this.accountRepository = accountRepository;
        this.activationTokenRepository = activationTokenRepository;
        this.notificationService = notificationService;
        this.activationBaseUrl = activationBaseUrl;
        this.activationExpiryHours = activationExpiryHours;
    }

    /**
     * {@inheritDoc}
     * Creates account with status PENDING and sends an activation email (US-ADM-01 / CS-07).
     *
     * @throws EmailAlreadyUsedException if the email is already registered
     */
    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException(request.getEmail());
        }

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        account.setRole(request.getRole());
        account.setStatus(AccountStatus.PENDING);
        accountRepository.save(account);

        issueActivationToken(account);

        return AccountResponse.from(account);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountResponse::from)
                .toList();
    }

    /**
     * Generates a fresh activation token for the account, replacing any previous one,
     * then sends the activation email.
     *
     * @param account the account to issue the token for
     */
    public void issueActivationToken(Account account) {
        activationTokenRepository.deleteByAccountId(account.getId());

        ActivationToken token = new ActivationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setAccount(account);
        token.setExpiresAt(LocalDateTime.now().plusHours(activationExpiryHours));
        activationTokenRepository.save(token);

        String link = activationBaseUrl + "/activate?token=" + token.getToken();
        notificationService.sendActivationEmail(account.getEmail(), link, LocaleContextHolder.getLocale());
    }
}
