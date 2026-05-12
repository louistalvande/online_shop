package com.shop.account.dto;

import com.shop.account.entity.AccountRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Payload for admin account creation (US-ADM-01). */
public class CreateAccountRequest {

    /** Email address — must be unique on the platform. */
    @Schema(description = "Email address, unique across the platform")
    @Email
    @NotBlank
    private String email;

    /** Initial plain-text password (stored hashed). */
    @Schema(description = "Initial plain-text password — will be hashed before storage")
    @NotBlank
    @Size(min = 8)
    private String password;

    /** First name of the account holder. */
    @Schema(description = "First name of the account holder")
    @NotBlank
    private String firstName;

    /** Last name of the account holder. */
    @Schema(description = "Last name of the account holder")
    @NotBlank
    private String lastName;

    /** Role to assign — BUYER or VENDOR (admin may not be created via this endpoint). */
    @Schema(description = "Role to assign: BUYER or VENDOR")
    @NotNull
    private AccountRole role;

    /** @return the email */
    public String getEmail() { return email; }

    /** @param email the email to set */
    public void setEmail(String email) { this.email = email; }

    /** @return the plain-text password */
    public String getPassword() { return password; }

    /** @param password the password to set */
    public void setPassword(String password) { this.password = password; }

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
}
