package com.shop.account.repository;

import com.shop.account.entity.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository for {@link DeliveryAddress} entities (US-PRF-03). */
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, UUID> {

    /**
     * Returns all non-deleted addresses belonging to the given account.
     *
     * @param accountId the owner account UUID
     * @return list of active delivery addresses ordered by creation date
     */
    List<DeliveryAddress> findByAccountIdAndDeletedFalseOrderByCreatedAtAsc(UUID accountId);

    /**
     * Finds a single non-deleted address by its id and owner account.
     *
     * @param id        the address UUID
     * @param accountId the owner account UUID
     * @return the address if found and not deleted
     */
    Optional<DeliveryAddress> findByIdAndAccountIdAndDeletedFalse(UUID id, UUID accountId);

    /**
     * Returns the number of non-deleted addresses for the given account.
     * Used to block deletion of the last active address.
     *
     * @param accountId the owner account UUID
     * @return count of active delivery addresses
     */
    long countByAccountIdAndDeletedFalse(UUID accountId);
}
