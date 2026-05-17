package com.shop.account.repository;

import com.shop.account.entity.Account;
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
}
