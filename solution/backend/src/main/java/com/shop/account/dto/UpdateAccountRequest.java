package com.shop.account.dto;

import com.shop.account.entity.AccountLanguage;
import com.shop.account.entity.AccountRole;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Payload to update the editable fields of a platform account (FS-A01 / FS-A02 / CS-10).
 * All fields are optional — only non-null values are applied (PATCH semantics).
 */
public class UpdateAccountRequest {

    /** New first name, or null to leave unchanged. */
    @Schema(description = "First name — null to leave unchanged")
    private String firstName;

    /** New last name, or null to leave unchanged. */
    @Schema(description = "Last name — null to leave unchanged")
    private String lastName;

    /** New role, or null to leave unchanged. */
    @Schema(description = "Platform role: BUYER or VENDOR — null to leave unchanged")
    private AccountRole role;

    /** New notification language, or null to leave unchanged. */
    @Schema(description = "Notification language: FR or EN — null to leave unchanged")
    private AccountLanguage language;

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
