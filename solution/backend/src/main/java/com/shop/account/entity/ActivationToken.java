package com.shop.account.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Single-use token sent by email when an account is created (US-ADM-01, US-REG-01 / CS-07). */
@Entity
@Table(name = "activation_tokens")
public class ActivationToken {

    /** UUID used as the URL token parameter. */
    @Id
    private String token;

    /** Account this token will activate. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** Expiry timestamp — 24 hours after generation (CS-07). */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** @return the token string */
    public String getToken() { return token; }

    /** @param token the token string to set */
    public void setToken(String token) { this.token = token; }

    /** @return the account to activate */
    public Account getAccount() { return account; }

    /** @param account the account to set */
    public void setAccount(Account account) { this.account = account; }

    /** @return the expiry timestamp */
    public LocalDateTime getExpiresAt() { return expiresAt; }

    /** @param expiresAt the expiry timestamp to set */
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
