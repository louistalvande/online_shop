package com.shop.account.service.impl;

import com.shop.account.dto.AccountResponse;
import com.shop.account.dto.ProfileResponse;
import com.shop.account.dto.UpdateProfileRequest;
import com.shop.account.entity.Account;
import com.shop.account.entity.AccountLanguage;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import com.shop.account.exception.AccountNotFoundException;
import com.shop.account.exception.InvalidAccountStateException;
import com.shop.account.exception.WrongCurrentPasswordException;
import com.shop.account.repository.AccountRepository;
import com.shop.account.repository.ActivationTokenRepository;
import com.shop.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    @Mock PasswordEncoder passwordEncoder;

    AccountServiceImpl service;

    private static final String EMAIL    = "alice@example.com";
    private static final String HASH     = "$2a$12$existingHash";
    private static final String PASSWORD = "OldPass12!";

    @BeforeEach
    void setUp() {
        service = new AccountServiceImpl(
                accountRepository,
                activationTokenRepository,
                notificationService,
                passwordEncoder,
                "http://localhost",
                24);
    }

    private Account activeAccount() {
        Account a = new Account();
        a.setEmail(EMAIL);
        a.setPasswordHash(HASH);
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
        given(accountRepository.findByStatusNot(AccountStatus.DELETED)).willReturn(List.of(activeAccount()));

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

    /** getProfile must return a ProfileResponse matching the stored account. */
    @Test
    void getProfile_returnsProfile() {
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(activeAccount()));

        ProfileResponse profile = service.getProfile(EMAIL);

        assertThat(profile.getEmail()).isEqualTo(EMAIL);
        assertThat(profile.getFirstName()).isEqualTo("Alice");
    }

    /** updateProfile with scalar fields must persist only the supplied values. */
    @Test
    void updateProfile_scalarFields_updatesAccount() {
        Account account = activeAccount();
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(account));
        given(accountRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("Bob");
        req.setPhone("0601020304");

        ProfileResponse result = service.updateProfile(EMAIL, req);

        assertThat(result.getFirstName()).isEqualTo("Bob");
        assertThat(result.getPhone()).isEqualTo("0601020304");
        assertThat(result.getLastName()).isEqualTo("Smith"); // unchanged
    }

    /** updateProfile with correct currentPassword must update the hash. */
    @Test
    void updateProfile_correctCurrentPassword_changesHash() {
        Account account = activeAccount();
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(account));
        given(accountRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(passwordEncoder.matches(PASSWORD, HASH)).willReturn(true);
        given(passwordEncoder.encode("NewPass12!")).willReturn("$2a$12$newHash");

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setCurrentPassword(PASSWORD);
        req.setNewPassword("NewPass12!");
        req.setConfirmPassword("NewPass12!");

        service.updateProfile(EMAIL, req);

        then(passwordEncoder).should().encode("NewPass12!");
        assertThat(account.getPasswordHash()).isEqualTo("$2a$12$newHash");
    }

    /** updateProfile with wrong currentPassword must throw WrongCurrentPasswordException. */
    @Test
    void updateProfile_wrongCurrentPassword_throws() {
        Account account = activeAccount();
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(account));
        given(passwordEncoder.matches("WrongPass!", HASH)).willReturn(false);

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setCurrentPassword("WrongPass!");
        req.setNewPassword("NewPass12!");
        req.setConfirmPassword("NewPass12!");

        assertThatThrownBy(() -> service.updateProfile(EMAIL, req))
                .isInstanceOf(WrongCurrentPasswordException.class);
        then(accountRepository).should(never()).save(any());
    }

    /** updateProfile with mismatched confirmPassword must throw IllegalArgumentException. */
    @Test
    void updateProfile_mismatchedPasswords_throws() {
        Account account = activeAccount();
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(account));
        given(passwordEncoder.matches(PASSWORD, HASH)).willReturn(true);

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setCurrentPassword(PASSWORD);
        req.setNewPassword("NewPass12!");
        req.setConfirmPassword("DifferentPass12!");

        assertThatThrownBy(() -> service.updateProfile(EMAIL, req))
                .isInstanceOf(IllegalArgumentException.class);
        then(accountRepository).should(never()).save(any());
    }

    /** getProfile for an admin must return a response without phone. */
    @Test
    void getProfile_adminAccount_omitsPhone() {
        Account admin = activeAccount();
        admin.setRole(AccountRole.ADMIN);
        admin.setPhone("0601020304");
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(admin));

        ProfileResponse profile = service.getProfile(EMAIL);

        assertThat(profile.getRole()).isEqualTo(AccountRole.ADMIN);
        assertThat(profile.getPhone()).isNull();
    }

    /** updateProfile must persist the language when changed to ES. */
    @Test
    void updateProfile_languageEs_updatesLanguage() {
        Account account = activeAccount(); // starts with AccountLanguage.FR
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(account));
        given(accountRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setLanguage(AccountLanguage.ES);

        ProfileResponse result = service.updateProfile(EMAIL, req);

        assertThat(result.getLanguage()).isEqualTo(AccountLanguage.ES);
        then(accountRepository).should().save(account);
    }

    /** updateProfile must not change language when the field is omitted. */
    @Test
    void updateProfile_nullLanguage_keepsExistingLanguage() {
        Account account = activeAccount(); // starts with AccountLanguage.FR
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(account));
        given(accountRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("Bob");
        // language not set → null

        ProfileResponse result = service.updateProfile(EMAIL, req);

        assertThat(result.getLanguage()).isEqualTo(AccountLanguage.FR);
    }

    /** updateProfile for an admin must ignore phone update. */
    @Test
    void updateProfile_adminAccount_ignoresPhone() {
        Account admin = activeAccount();
        admin.setRole(AccountRole.ADMIN);
        given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(admin));
        given(accountRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("Bob");
        req.setPhone("0601020304");

        ProfileResponse result = service.updateProfile(EMAIL, req);

        assertThat(result.getFirstName()).isEqualTo("Bob");
        assertThat(result.getPhone()).isNull();
        assertThat(admin.getPhone()).isNull();
    }
}
