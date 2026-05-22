package com.shop.account.dto;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountLanguage;
import com.shop.account.entity.AccountRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/** Read-only representation of the authenticated user's own profile (US-PRF-01, US-PRF-02). */
public class ProfileResponse {

    @Schema(description = "Account UUID") private UUID id;
    @Schema(description = "Email address — read-only, cannot be changed") private String email;
    @Schema(description = "Given name") private String firstName;
    @Schema(description = "Family name") private String lastName;
    @Schema(description = "Phone number") private String phone;
    @Schema(description = "Preferred notification language") private AccountLanguage language;
    @Schema(description = "Account role") private AccountRole role;
    @Schema(description = "Account creation timestamp") private LocalDateTime createdAt;

    /**
     * Creates a {@link ProfileResponse} from an {@link Account} entity.
     * Use for non-admin accounts (buyers, vendors) — includes phone.
     *
     * @param account the source entity
     * @return the DTO with all fields populated
     */
    public static ProfileResponse from(Account account) {
        ProfileResponse r = new ProfileResponse();
        r.id        = account.getId();
        r.email     = account.getEmail();
        r.firstName = account.getFirstName();
        r.lastName  = account.getLastName();
        r.phone     = account.getPhone();
        r.language  = account.getLanguage();
        r.role      = account.getRole();
        r.createdAt = account.getCreatedAt();
        return r;
    }

    /**
     * Creates a {@link ProfileResponse} for an admin account, omitting phone.
     * Admins do not have contact phone on their profile.
     *
     * @param account the admin account entity
     * @return the DTO without phone
     */
    public static ProfileResponse fromAdmin(Account account) {
        ProfileResponse r = new ProfileResponse();
        r.id        = account.getId();
        r.email     = account.getEmail();
        r.firstName = account.getFirstName();
        r.lastName  = account.getLastName();
        r.language  = account.getLanguage();
        r.role      = account.getRole();
        r.createdAt = account.getCreatedAt();
        return r;
    }

    /** @return the account UUID */
    public UUID getId() { return id; }

    /** @return the email address */
    public String getEmail() { return email; }

    /** @return the given name */
    public String getFirstName() { return firstName; }

    /** @return the family name */
    public String getLastName() { return lastName; }

    /** @return the phone number, or {@code null} */
    public String getPhone() { return phone; }

    /** @return the preferred notification language */
    public AccountLanguage getLanguage() { return language; }

    /** @return the account role */
    public AccountRole getRole() { return role; }

    /** @return the creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }
}
