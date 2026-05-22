package com.shop.account.service.impl;

import com.shop.account.dto.DeliveryAddressRequest;
import com.shop.account.dto.DeliveryAddressResponse;
import com.shop.account.entity.Account;
import com.shop.account.entity.DeliveryAddress;
import com.shop.account.exception.AccountNotFoundException;
import com.shop.account.exception.DeliveryAddressNotFoundException;
import com.shop.account.exception.LastActiveAddressException;
import com.shop.account.repository.AccountRepository;
import com.shop.account.repository.DeliveryAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link DeliveryAddressServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class DeliveryAddressServiceImplTest {

    @Mock DeliveryAddressRepository addressRepository;
    @Mock AccountRepository accountRepository;

    DeliveryAddressServiceImpl service;

    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final String BUYER_EMAIL = "buyer@test.com";
    private static final UUID ADDRESS_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new DeliveryAddressServiceImpl(addressRepository, accountRepository);
    }

    // ─── listAddresses ────────────────────────────────────────────────────────

    @Test
    void listAddresses_returnsMappedDtos() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buildAccount()));
        DeliveryAddress addr = buildAddress(ADDRESS_ID, true);
        given(addressRepository.findByAccountIdAndDeletedFalseOrderByCreatedAtAsc(ACCOUNT_ID))
                .willReturn(List.of(addr));

        List<DeliveryAddressResponse> result = service.listAddresses(BUYER_EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(ADDRESS_ID);
        assertThat(result.get(0).isDefault()).isTrue();
    }

    @Test
    void listAddresses_returnsEmptyList() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buildAccount()));
        given(addressRepository.findByAccountIdAndDeletedFalseOrderByCreatedAtAsc(ACCOUNT_ID))
                .willReturn(List.of());

        assertThat(service.listAddresses(BUYER_EMAIL)).isEmpty();
    }

    @Test
    void listAddresses_accountNotFound_throws() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.listAddresses(BUYER_EMAIL))
                .isInstanceOf(AccountNotFoundException.class);
    }

    // ─── createAddress ────────────────────────────────────────────────────────

    @Test
    void createAddress_firstAddress_isAutomaticallyDefault() {
        Account account = buildAccount();
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(account));
        given(addressRepository.countByAccountIdAndDeletedFalse(ACCOUNT_ID)).willReturn(0L);
        given(addressRepository.findByAccountIdAndDeletedFalseOrderByCreatedAtAsc(ACCOUNT_ID))
                .willReturn(List.of());
        DeliveryAddress saved = buildAddress(ADDRESS_ID, true);
        given(addressRepository.save(any())).willReturn(saved);

        DeliveryAddressResponse result = service.createAddress(BUYER_EMAIL, buildRequest(false));

        assertThat(result.isDefault()).isTrue();
    }

    @Test
    void createAddress_makeDefault_clearsExistingDefault() {
        Account account = buildAccount();
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(account));
        given(addressRepository.countByAccountIdAndDeletedFalse(ACCOUNT_ID)).willReturn(1L);
        DeliveryAddress existing = buildAddress(UUID.randomUUID(), true);
        given(addressRepository.findByAccountIdAndDeletedFalseOrderByCreatedAtAsc(ACCOUNT_ID))
                .willReturn(new ArrayList<>(List.of(existing)));
        DeliveryAddress saved = buildAddress(ADDRESS_ID, true);
        given(addressRepository.save(any())).willReturn(existing, saved);

        service.createAddress(BUYER_EMAIL, buildRequest(true));

        assertThat(existing.isDefault()).isFalse();
    }

    @Test
    void createAddress_notMakeDefault_doesNotClearExisting() {
        Account account = buildAccount();
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(account));
        given(addressRepository.countByAccountIdAndDeletedFalse(ACCOUNT_ID)).willReturn(1L);
        DeliveryAddress saved = buildAddress(ADDRESS_ID, false);
        given(addressRepository.save(any())).willReturn(saved);

        DeliveryAddressResponse result = service.createAddress(BUYER_EMAIL, buildRequest(false));

        assertThat(result.isDefault()).isFalse();
        then(addressRepository).should(org.mockito.Mockito.never())
                .findByAccountIdAndDeletedFalseOrderByCreatedAtAsc(ACCOUNT_ID);
    }

    // ─── updateAddress ────────────────────────────────────────────────────────

    @Test
    void updateAddress_updatesFieldsAndReturnsDto() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buildAccount()));
        DeliveryAddress addr = buildAddress(ADDRESS_ID, false);
        given(addressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, ACCOUNT_ID))
                .willReturn(Optional.of(addr));
        given(addressRepository.save(any())).willReturn(addr);

        DeliveryAddressRequest req = buildRequest(false);
        req.setLabel("Office");
        service.updateAddress(BUYER_EMAIL, ADDRESS_ID, req);

        assertThat(addr.getLabel()).isEqualTo("Office");
    }

    @Test
    void updateAddress_promotesToDefault_clearsExisting() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buildAccount()));
        DeliveryAddress addr = buildAddress(ADDRESS_ID, false);
        given(addressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, ACCOUNT_ID))
                .willReturn(Optional.of(addr));
        DeliveryAddress previous = buildAddress(UUID.randomUUID(), true);
        given(addressRepository.findByAccountIdAndDeletedFalseOrderByCreatedAtAsc(ACCOUNT_ID))
                .willReturn(new ArrayList<>(List.of(previous, addr)));
        given(addressRepository.save(any())).willReturn(previous, addr);

        service.updateAddress(BUYER_EMAIL, ADDRESS_ID, buildRequest(true));

        assertThat(previous.isDefault()).isFalse();
        assertThat(addr.isDefault()).isTrue();
    }

    @Test
    void updateAddress_notFound_throws() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buildAccount()));
        given(addressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, ACCOUNT_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateAddress(BUYER_EMAIL, ADDRESS_ID, buildRequest(false)))
                .isInstanceOf(DeliveryAddressNotFoundException.class);
    }

    // ─── deleteAddress ────────────────────────────────────────────────────────

    @Test
    void deleteAddress_softDeletesNonDefault() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buildAccount()));
        DeliveryAddress addr = buildAddress(ADDRESS_ID, false);
        given(addressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, ACCOUNT_ID))
                .willReturn(Optional.of(addr));
        given(addressRepository.countByAccountIdAndDeletedFalse(ACCOUNT_ID)).willReturn(2L);

        service.deleteAddress(BUYER_EMAIL, ADDRESS_ID);

        assertThat(addr.isDeleted()).isTrue();
        then(addressRepository).should().save(addr);
    }

    @Test
    void deleteAddress_lastAddress_throwsLastActiveAddressException() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buildAccount()));
        given(addressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, ACCOUNT_ID))
                .willReturn(Optional.of(buildAddress(ADDRESS_ID, true)));
        given(addressRepository.countByAccountIdAndDeletedFalse(ACCOUNT_ID)).willReturn(1L);

        assertThatThrownBy(() -> service.deleteAddress(BUYER_EMAIL, ADDRESS_ID))
                .isInstanceOf(LastActiveAddressException.class);
    }

    @Test
    void deleteAddress_defaultAddress_reassignsDefaultToNext() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buildAccount()));
        DeliveryAddress defaultAddr = buildAddress(ADDRESS_ID, true);
        UUID nextId = UUID.randomUUID();
        DeliveryAddress nextAddr = buildAddress(nextId, false);
        given(addressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, ACCOUNT_ID))
                .willReturn(Optional.of(defaultAddr));
        given(addressRepository.countByAccountIdAndDeletedFalse(ACCOUNT_ID)).willReturn(2L);
        given(addressRepository.findByAccountIdAndDeletedFalseOrderByCreatedAtAsc(ACCOUNT_ID))
                .willReturn(new ArrayList<>(List.of(nextAddr)));
        given(addressRepository.save(any())).willReturn(defaultAddr, nextAddr, defaultAddr);

        service.deleteAddress(BUYER_EMAIL, ADDRESS_ID);

        assertThat(nextAddr.isDefault()).isTrue();
        assertThat(defaultAddr.isDeleted()).isTrue();
    }

    @Test
    void deleteAddress_notFound_throws() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buildAccount()));
        given(addressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, ACCOUNT_ID))
                .willReturn(Optional.empty());
        given(addressRepository.countByAccountIdAndDeletedFalse(ACCOUNT_ID)).willReturn(2L);

        assertThatThrownBy(() -> service.deleteAddress(BUYER_EMAIL, ADDRESS_ID))
                .isInstanceOf(DeliveryAddressNotFoundException.class);
    }

    // ─── setDefault ───────────────────────────────────────────────────────────

    @Test
    void setDefault_clearsOldAndSetsNew() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buildAccount()));
        DeliveryAddress addr = buildAddress(ADDRESS_ID, false);
        given(addressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, ACCOUNT_ID))
                .willReturn(Optional.of(addr));
        DeliveryAddress previous = buildAddress(UUID.randomUUID(), true);
        given(addressRepository.findByAccountIdAndDeletedFalseOrderByCreatedAtAsc(ACCOUNT_ID))
                .willReturn(new ArrayList<>(List.of(previous, addr)));
        given(addressRepository.save(any())).willReturn(previous, addr);

        service.setDefault(BUYER_EMAIL, ADDRESS_ID);

        assertThat(previous.isDefault()).isFalse();
        assertThat(addr.isDefault()).isTrue();
    }

    @Test
    void setDefault_notFound_throws() {
        given(accountRepository.findByEmail(BUYER_EMAIL)).willReturn(Optional.of(buildAccount()));
        given(addressRepository.findByIdAndAccountIdAndDeletedFalse(ADDRESS_ID, ACCOUNT_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.setDefault(BUYER_EMAIL, ADDRESS_ID))
                .isInstanceOf(DeliveryAddressNotFoundException.class);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Account buildAccount() {
        Account a = new Account();
        setField(a, "id", ACCOUNT_ID);
        a.setEmail(BUYER_EMAIL);
        return a;
    }

    private DeliveryAddress buildAddress(UUID id, boolean isDefault) {
        Account owner = buildAccount();
        DeliveryAddress a = new DeliveryAddress();
        setField(a, "id", id);
        a.setAccount(owner);
        a.setLabel("Home");
        a.setAddressLine("1 rue Test");
        a.setCity("Paris");
        a.setPostalCode("75001");
        a.setCountryCode("FR");
        a.setDefault(isDefault);
        return a;
    }

    private DeliveryAddressRequest buildRequest(boolean makeDefault) {
        DeliveryAddressRequest r = new DeliveryAddressRequest();
        r.setLabel("Home");
        r.setAddressLine("1 rue Test");
        r.setCity("Paris");
        r.setPostalCode("75001");
        r.setCountryCode("FR");
        r.setMakeDefault(makeDefault);
        return r;
    }

    private static void setField(Object target, String name, Object value) {
        try {
            var f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
