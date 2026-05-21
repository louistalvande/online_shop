package com.shop.auth.entity;

import com.shop.account.entity.Account;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

/** Single-use, time-limited token for password recovery (SEC-PWD-006 / CPA-17). */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    /** UUID v4 used as the URL token parameter. */
    @Id
    @Column(length = 36)
    private String token;

    /** Account whose password this token can reset. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** Expiry timestamp — 1 hour after generation (CPA-17). */
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    /** Whether this token has already been consumed. */
    @Column(nullable = false)
    private boolean used = false;

    /** @return the token string */
    public String getToken() { return token; }

    /** @param token the token string to set */
    public void setToken(String token) { this.token = token; }

    /** @return the account to reset the password for */
    public Account getAccount() { return account; }

    /** @param account the account to set */
    public void setAccount(Account account) { this.account = account; }

    /** @return the expiry timestamp */
    public OffsetDateTime getExpiresAt() { return expiresAt; }

    /** @param expiresAt the expiry timestamp to set */
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    /** @return {@code true} if this token has already been used */
    public boolean isUsed() { return used; }

    /** @param used whether the token has been consumed */
    public void setUsed(boolean used) { this.used = used; }
}
