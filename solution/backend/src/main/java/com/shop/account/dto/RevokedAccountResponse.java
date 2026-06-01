package com.shop.account.dto;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/** Read-only view of an account awaiting password renewal after revocation (US-SEC-04). */
public class RevokedAccountResponse {

    @Schema(description = "Account UUID") private String id;
    @Schema(description = "Email address") private String email;
    @Schema(description = "Given name") private String firstName;
    @Schema(description = "Family name") private String lastName;
    @Schema(description = "Account role") private AccountRole role;
    @Schema(description = "Account status") private AccountStatus status;
    @Schema(description = "Timestamp when the password was administratively revoked") private OffsetDateTime revokedAt;
    @Schema(description = "Number of whole hours elapsed since the password was revoked") private long hoursSinceRevocation;

    /**
     * Builds a response from an {@link Account} entity.
     *
     * @param account the revoked account
     * @return the DTO
     */
    public static RevokedAccountResponse from(Account account) {
        RevokedAccountResponse r = new RevokedAccountResponse();
        r.id                   = account.getId().toString();
        r.email                = account.getEmail();
        r.firstName            = account.getFirstName();
        r.lastName             = account.getLastName();
        r.role                 = account.getRole();
        r.status               = account.getStatus();
        r.revokedAt            = account.getPasswordRevokedAt();
        r.hoursSinceRevocation = account.getPasswordRevokedAt() != null
                ? ChronoUnit.HOURS.between(account.getPasswordRevokedAt(), OffsetDateTime.now())
                : 0;
        return r;
    }

    /** @return the account UUID as a string */
    public String getId() { return id; }
    /** @return the email address */
    public String getEmail() { return email; }
    /** @return the given name */
    public String getFirstName() { return firstName; }
    /** @return the family name */
    public String getLastName() { return lastName; }
    /** @return the account role */
    public AccountRole getRole() { return role; }
    /** @return the account status */
    public AccountStatus getStatus() { return status; }
    /** @return the revocation timestamp */
    public OffsetDateTime getRevokedAt() { return revokedAt; }
    /** @return hours elapsed since revocation */
    public long getHoursSinceRevocation() { return hoursSinceRevocation; }
}
