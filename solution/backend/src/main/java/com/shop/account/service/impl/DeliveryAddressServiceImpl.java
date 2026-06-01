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
import com.shop.account.service.DeliveryAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Default implementation of {@link DeliveryAddressService}. */
@Service
@Transactional
public class DeliveryAddressServiceImpl implements DeliveryAddressService {

    private final DeliveryAddressRepository addressRepository;
    private final AccountRepository accountRepository;

    /**
     * Constructs the service with its required repositories.
     *
     * @param addressRepository repository for delivery addresses
     * @param accountRepository repository for buyer accounts (used to resolve email → UUID)
     */
    public DeliveryAddressServiceImpl(DeliveryAddressRepository addressRepository,
                                      AccountRepository accountRepository) {
        this.addressRepository = addressRepository;
        this.accountRepository = accountRepository;
    }

    /** {@inheritDoc} */
    @Override
    public List<DeliveryAddressResponse> listAddresses(String buyerEmail) {
        UUID accountId = resolveAccountId(buyerEmail);
        return addressRepository.findByAccountIdAndDeletedFalseOrderByCreatedAtAsc(accountId)
                .stream()
                .map(DeliveryAddressResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public DeliveryAddressResponse createAddress(String buyerEmail, DeliveryAddressRequest request) {
        Account account = accountRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new AccountNotFoundException(buyerEmail));
        UUID accountId = account.getId();
        boolean isFirst = addressRepository.countByAccountIdAndDeletedFalse(accountId) == 0;

        if (request.isMakeDefault() || isFirst) {
            clearDefault(accountId);
        }

        DeliveryAddress address = new DeliveryAddress();
        address.setAccount(account);
        populateFields(address, request);
        address.setDefault(request.isMakeDefault() || isFirst);

        return DeliveryAddressResponse.from(addressRepository.save(address));
    }

    /** {@inheritDoc} */
    @Override
    public DeliveryAddressResponse updateAddress(String buyerEmail, UUID addressId, DeliveryAddressRequest request) {
        UUID accountId = resolveAccountId(buyerEmail);
        DeliveryAddress address = findOrThrow(addressId, accountId);

        if (request.isMakeDefault() && !address.isDefault()) {
            clearDefault(accountId);
        }

        populateFields(address, request);
        if (request.isMakeDefault()) {
            address.setDefault(true);
        }

        return DeliveryAddressResponse.from(addressRepository.save(address));
    }

    /** {@inheritDoc} */
    @Override
    public void deleteAddress(String buyerEmail, UUID addressId) {
        UUID accountId = resolveAccountId(buyerEmail);
        DeliveryAddress address = findOrThrow(addressId, accountId);

        if (addressRepository.countByAccountIdAndDeletedFalse(accountId) <= 1) {
            throw new LastActiveAddressException();
        }

        if (address.isDefault()) {
            address.setDefault(false);
            addressRepository.save(address);
            List<DeliveryAddress> remaining = addressRepository
                    .findByAccountIdAndDeletedFalseOrderByCreatedAtAsc(accountId);
            if (!remaining.isEmpty()) {
                DeliveryAddress next = remaining.stream()
                        .filter(a -> !a.getId().equals(addressId))
                        .findFirst()
                        .orElse(null);
                if (next != null) {
                    next.setDefault(true);
                    addressRepository.save(next);
                }
            }
        }

        address.setDeleted(true);
        addressRepository.save(address);
    }

    /** {@inheritDoc} */
    @Override
    public DeliveryAddressResponse setDefault(String buyerEmail, UUID addressId) {
        UUID accountId = resolveAccountId(buyerEmail);
        DeliveryAddress address = findOrThrow(addressId, accountId);

        clearDefault(accountId);
        address.setDefault(true);
        return DeliveryAddressResponse.from(addressRepository.save(address));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private UUID resolveAccountId(String email) {
        return accountRepository.findByEmail(email)
                .map(Account::getId)
                .orElseThrow(() -> new AccountNotFoundException(email));
    }

    private DeliveryAddress findOrThrow(UUID addressId, UUID accountId) {
        return addressRepository.findByIdAndAccountIdAndDeletedFalse(addressId, accountId)
                .orElseThrow(() -> new DeliveryAddressNotFoundException(addressId));
    }

    private void clearDefault(UUID accountId) {
        addressRepository.findByAccountIdAndDeletedFalseOrderByCreatedAtAsc(accountId)
                .stream()
                .filter(DeliveryAddress::isDefault)
                .forEach(a -> {
                    a.setDefault(false);
                    addressRepository.save(a);
                });
    }

    private void populateFields(DeliveryAddress address, DeliveryAddressRequest request) {
        address.setLabel(request.getLabel());
        address.setRecipientName(request.getRecipientName());
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setPostalCode(request.getPostalCode());
        address.setCountryCode(request.getCountryCode());
    }
}
