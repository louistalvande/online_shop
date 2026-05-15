package com.shop.account.repository;

import com.shop.account.entity.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Data access layer for {@link ActivationToken} entities. */
public interface ActivationTokenRepository extends JpaRepository<ActivationToken, String> {

    /**
     * Finds a token by its string value.
     *
     * @param token the token string
     * @return the matching token, or empty if not found
     */
    Optional<ActivationToken> findByToken(String token);

    /**
     * Deletes all tokens for a given account.
     * Called on successful activation and when the admin resends a link (UCSA-12 — 2b).
     *
     * @param accountId the account UUID
     */
    void deleteByAccountId(UUID accountId);
}
