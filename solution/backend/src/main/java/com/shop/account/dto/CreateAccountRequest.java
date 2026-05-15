package com.shop.account.dto;

import com.shop.account.entity.AccountLanguage;
import com.shop.account.entity.AccountRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Payload for admin account creation (US-ADM-01). No password — user sets it via activation email. */
public class CreateAccountRequest {

    /** Email address — must be unique on the platform. */
    @Schema(description = "Email address, unique across the platform")
    @Email
    @NotBlank
    private String email;

    /** First name of the account holder. */
    @Schema(description = "First name of the account holder")
    @NotBlank
    private String firstName;

    /** Last name of the account holder. */
    @Schema(description = "Last name of the account holder")
    @NotBlank
    private String lastName;

    /** Role to assign — BUYER or VENDOR (ADMIN may not be created via this endpoint). */
    @Schema(description = "Role to assign: BUYER or VENDOR")
    @NotNull
    private AccountRole role;

    /** Preferred notification language — defaults to FR if absent (CS-10). */
    @Schema(description = "Notification language: FR (French, default) or EN (English)")
    private AccountLanguage language = AccountLanguage.FR;

    /** @return the email */
    public String getEmail() { return email; }

    /** @param email the email to set */
    public void setEmail(String email) { this.email = email; }

    /** @return the first name */
    public String getFirstName() { return firstName; }

    /** @param firstName the first name to set */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** @return the last name */
    public String getLastName() { return lastName; }

    /** @param lastName the last name to set */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /** @return the role */
    public AccountRole getRole() { return role; }

    /** @param role the role to set */
    public void setRole(AccountRole role) { this.role = role; }

    /** @return the notification language */
    public AccountLanguage getLanguage() { return language; }

    /** @param language the notification language to set */
    public void setLanguage(AccountLanguage language) { this.language = language; }
}
