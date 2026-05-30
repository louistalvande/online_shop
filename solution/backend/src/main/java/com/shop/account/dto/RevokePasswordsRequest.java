package com.shop.account.dto;

import com.shop.account.entity.AccountRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Payload for the bulk password-revocation endpoint (US-SEC-04 / FS-S11 / CPA-17).
 * At least one of {@code role} or {@code emails} must be provided.
 */
public class RevokePasswordsRequest {

    @Schema(description = "Revoke passwords for all active accounts with this role — mutually inclusive with emails")
    private AccountRole role;

    @Schema(description = "Explicit list of email addresses whose passwords must be revoked")
    private List<String> emails;

    /** @return the role to target, or {@code null} if not specified */
    public AccountRole getRole() { return role; }

    /** @param role the role to target */
    public void setRole(AccountRole role) { this.role = role; }

    /** @return the explicit email list, or {@code null} if not specified */
    public List<String> getEmails() { return emails; }

    /** @param emails the email addresses to target */
    public void setEmails(List<String> emails) { this.emails = emails; }
}
