package com.shop.claim.repository;

import com.shop.claim.entity.Claim;
import com.shop.claim.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JPA repository for {@link Claim} entities. */
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    /**
     * Returns all claims for a given buyer, ordered from newest to oldest.
     *
     * @param buyerId the buyer account UUID
     * @return list of claims belonging to the buyer
     */
    List<Claim> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);

    /**
     * Returns all claims for a given vendor, ordered from newest to oldest.
     *
     * @param vendorId the vendor account UUID
     * @return list of claims belonging to the vendor
     */
    List<Claim> findByVendorIdOrderByCreatedAtDesc(UUID vendorId);

    /**
     * Returns a claim by its ID only if it belongs to the specified vendor.
     * Used for vendor ownership enforcement.
     *
     * @param id       the claim UUID
     * @param vendorId the vendor account UUID
     * @return an optional containing the claim, or empty if not found or not owned
     */
    Optional<Claim> findByIdAndVendorId(UUID id, UUID vendorId);

    /**
     * Returns a claim by its ID only if it belongs to the specified buyer.
     * Used for buyer ownership enforcement.
     *
     * @param id      the claim UUID
     * @param buyerId the buyer account UUID
     * @return an optional containing the claim, or empty if not found or not owned
     */
    Optional<Claim> findByIdAndBuyerId(UUID id, UUID buyerId);

    /**
     * Checks whether an OPEN claim already exists for a given order and buyer.
     * Used to prevent duplicate claims.
     *
     * @param orderId  the order UUID
     * @param buyerId  the buyer account UUID
     * @param status   the claim status to check (typically {@link ClaimStatus#OPEN})
     * @return {@code true} if such a claim exists
     */
    boolean existsByOrderIdAndBuyerIdAndStatus(UUID orderId, UUID buyerId, ClaimStatus status);
}
