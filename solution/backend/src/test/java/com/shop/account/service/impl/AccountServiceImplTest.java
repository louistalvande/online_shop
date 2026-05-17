package com.shop.account.service.impl;

import com.shop.account.dto.AccountResponse;
import com.shop.account.entity.Account;
import com.shop.account.entity.AccountLanguage;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import com.shop.account.exception.AccountNotFoundException;
import com.shop.account.exception.InvalidAccountStateException;
import com.shop.account.repository.AccountRepository;
import com.shop.account.repository.ActivationTokenRepository;
import com.shop.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

/** Unit tests for {@link AccountServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock AccountRepository accountRepository;
    @Mock ActivationTokenRepository activationTokenRepository;
    @Mock NotificationService notificationService;

    AccountServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountServiceImpl(
                accountRepository,
                activationTokenRepository,
                notificationService,
                "http://localhost",
                24);
    }

    private Account activeAccount() {
        Account a = new Account();
        a.setEmail("alice@example.com");
        a.setFirstName("Alice");
        a.setLastName("Smith");
        a.setRole(AccountRole.BUYER);
        a.setStatus(AccountStatus.ACTIVE);
        a.setLanguage(AccountLanguage.FR);
        return a;
    }

    /** listAccounts must delegate to findByStatusNot(DELETED) and never call findAll. */
    @Test
    void listAccounts_callsFindByStatusNotDeleted() {
        Account active = activeAccount();
        given(accountRepository.findByStatusNot(AccountStatus.DELETED)).willReturn(List.of(active));

        List<AccountResponse> result = service.listAccounts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(AccountStatus.ACTIVE);
        then(accountRepository).should().findByStatusNot(AccountStatus.DELETED);
        then(accountRepository).should(never()).findAll();
    }

    /** deleteAccount must set status to DELETED and persist the account. */
    @Test
    void deleteAccount_setsStatusDeleted_andSaves() {
        UUID id = UUID.randomUUID();
        Account account = activeAccount();
        given(accountRepository.findById(id)).willReturn(Optional.of(account));

        service.deleteAccount(id);

        assertThat(account.getStatus()).isEqualTo(AccountStatus.DELETED);
        then(accountRepository).should().save(account);
    }

    /** deleteAccount must throw AccountNotFoundException when no account matches the id. */
    @Test
    void deleteAccount_throwsAccountNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        given(accountRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteAccount(id))
                .isInstanceOf(AccountNotFoundException.class);
        then(accountRepository).should(never()).save(any());
    }

    /** suspendAccount must set status to SUSPENDED and persist the account. */
    @Test
    void suspendAccount_setsStatusSuspended_andSaves() {
        UUID id = UUID.randomUUID();
        Account account = activeAccount();
        given(accountRepository.findById(id)).willReturn(Optional.of(account));
        given(accountRepository.save(account)).willReturn(account);

        AccountResponse result = service.suspendAccount(id);

        assertThat(result.getStatus()).isEqualTo(AccountStatus.SUSPENDED);
        then(accountRepository).should().save(account);
    }

    /** suspendAccount must throw InvalidAccountStateException when the account is not ACTIVE. */
    @Test
    void suspendAccount_throwsInvalidAccountStateException_whenNotActive() {
        UUID id = UUID.randomUUID();
        Account account = activeAccount();
        account.setStatus(AccountStatus.SUSPENDED);
        given(accountRepository.findById(id)).willReturn(Optional.of(account));

        assertThatThrownBy(() -> service.suspendAccount(id))
                .isInstanceOf(InvalidAccountStateException.class);
        then(accountRepository).should(never()).save(any());
    }

    /** suspendAccount must throw AccountNotFoundException when no account matches the id. */
    @Test
    void suspendAccount_throwsAccountNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        given(accountRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.suspendAccount(id))
                .isInstanceOf(AccountNotFoundException.class);
        then(accountRepository).should(never()).save(any());
    }

    /** reactivateAccount must set status to ACTIVE and persist the account. */
    @Test
    void reactivateAccount_setsStatusActive_andSaves() {
        UUID id = UUID.randomUUID();
        Account account = activeAccount();
        account.setStatus(AccountStatus.SUSPENDED);
        given(accountRepository.findById(id)).willReturn(Optional.of(account));
        given(accountRepository.save(account)).willReturn(account);

        AccountResponse result = service.reactivateAccount(id);

        assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        then(accountRepository).should().save(account);
    }

    /** reactivateAccount must throw InvalidAccountStateException when the account is not SUSPENDED. */
    @Test
    void reactivateAccount_throwsInvalidAccountStateException_whenNotSuspended() {
        UUID id = UUID.randomUUID();
        Account account = activeAccount();
        given(accountRepository.findById(id)).willReturn(Optional.of(account));

        assertThatThrownBy(() -> service.reactivateAccount(id))
                .isInstanceOf(InvalidAccountStateException.class);
        then(accountRepository).should(never()).save(any());
    }

    /** reactivateAccount must throw AccountNotFoundException when no account matches the id. */
    @Test
    void reactivateAccount_throwsAccountNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        given(accountRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.reactivateAccount(id))
                .isInstanceOf(AccountNotFoundException.class);
        then(accountRepository).should(never()).save(any());
    }
}
