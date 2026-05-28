package com.shop.account.repository;

import com.shop.account.entity.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * Deletes all tokens for a given account via a direct SQL DELETE.
     * Using @Modifying + @Query avoids Spring Data's SELECT-then-delete pattern,
     * which causes StaleObjectStateException when concurrent requests consume the same token.
     *
     * @param accountId the account UUID
     */
    @Modifying
    @Query("DELETE FROM ActivationToken t WHERE t.account.id = :accountId")
    void deleteByAccountId(@Param("accountId") UUID accountId);
}
