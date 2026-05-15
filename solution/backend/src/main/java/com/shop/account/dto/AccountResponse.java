package com.shop.account.dto;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountLanguage;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/** Public representation of a platform account — never exposes the password hash. */
public class AccountResponse {

    /** Unique identifier of the account. */
    @Schema(description = "Unique account identifier")
    private UUID id;

    /** Email address. */
    @Schema(description = "Email address")
    private String email;

    /** First name. */
    @Schema(description = "First name")
    private String firstName;

    /** Last name. */
    @Schema(description = "Last name")
    private String lastName;

    /** Role on the platform. */
    @Schema(description = "Platform role")
    private AccountRole role;

    /** Lifecycle status. */
    @Schema(description = "Account status")
    private AccountStatus status;

    /** Preferred notification language (CS-10). */
    @Schema(description = "Notification language preference: FR or EN")
    private AccountLanguage language;

    /** Creation timestamp. */
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    /**
     * Builds a response DTO from a persisted entity.
     *
     * @param account the entity to convert
     * @return the corresponding response DTO
     */
    public static AccountResponse from(Account account) {
        AccountResponse r = new AccountResponse();
        r.id        = account.getId();
        r.email     = account.getEmail();
        r.firstName = account.getFirstName();
        r.lastName  = account.getLastName();
        r.role      = account.getRole();
        r.status    = account.getStatus();
        r.language  = account.getLanguage();
        r.createdAt = account.getCreatedAt();
        return r;
    }

    /** @return the account id */
    public UUID getId() { return id; }

    /** @return the email */
    public String getEmail() { return email; }

    /** @return the first name */
    public String getFirstName() { return firstName; }

    /** @return the last name */
    public String getLastName() { return lastName; }

    /** @return the role */
    public AccountRole getRole() { return role; }

    /** @return the status */
    public AccountStatus getStatus() { return status; }

    /** @return the notification language */
    public AccountLanguage getLanguage() { return language; }

    /** @return the creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }
}
