package com.shop.auth.repository;

import com.shop.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Data access layer for {@link PasswordResetToken} entities. */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

    /**
     * Finds a token by its string value.
     *
     * @param token the token string
     * @return the matching token, or empty if not found
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Deletes all reset tokens for a given account.
     * Called before issuing a new token to ensure only one active token exists per account.
     *
     * @param accountId the account UUID
     */
    void deleteByAccountId(UUID accountId);
}
