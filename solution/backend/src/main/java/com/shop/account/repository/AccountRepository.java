package com.shop.account.repository;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Data access layer for {@link Account} entities. */
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Finds an account by its email address.
     *
     * @param email the email to look up
     * @return the matching account, or empty if none
     */
    Optional<Account> findByEmail(String email);

    /**
     * Checks whether an account exists with the given email.
     *
     * @param email the email to check
     * @return {@code true} if an account with this email already exists
     */
    boolean existsByEmail(String email);

    /**
     * Returns all accounts whose status is not the given value.
     * Used by {@code listAccounts} to exclude DELETED accounts without loading them into memory.
     *
     * @param status the status to exclude
     * @return all accounts with a different status
     */
    List<Account> findByStatusNot(AccountStatus status);

    /**
     * Returns all accounts with the given role.
     * Used to broadcast notifications to all vendors when a new order is placed.
     *
     * @param role the role to filter by
     * @return all accounts holding that role
     */
    List<Account> findAllByRole(AccountRole role);

    /**
     * Returns all accounts with the given role and status where marketing consent is active.
     * Used to build the mailing list CSV export (US-PRF-05 / RGPD-CONS-004).
     *
     * @param role   the role to filter by (typically BUYER)
     * @param status the lifecycle status to filter by (typically ACTIVE)
     * @return accounts that have opted in to marketing emails
     */
    List<Account> findByRoleAndStatusAndMarketingConsentTrue(AccountRole role, AccountStatus status);

    /**
     * Returns all accounts whose password has been revoked and whose status is not DELETED.
     * Used by the admin view and the auto-suspension job (US-SEC-04 / FS-S11).
     *
     * @param status the status to exclude (DELETED)
     * @return revoked accounts awaiting password renewal
     */
    List<Account> findByPasswordRevokedTrueAndStatusNot(AccountStatus status);

    /**
     * Returns all accounts of the given role whose status is not the given value.
     * Used to target all active members of a role for bulk password revocation (US-SEC-04).
     *
     * @param role   the role to filter by
     * @param status the status to exclude (DELETED)
     * @return active accounts holding that role
     */
    List<Account> findByRoleAndStatusNot(AccountRole role, AccountStatus status);
}
