package com.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload for buyer self-registration (US-REG-01 / FS-B01). Role is always BUYER — set server-side. */
public class RegisterRequest {

    /** Email address — must be unique on the platform. */
    @Schema(description = "Email address, unique across the platform")
    @Email
    @NotBlank
    private String email;

    /** Chosen password — hashed before storage. */
    @Schema(description = "Chosen password — minimum 8 characters")
    @NotBlank
    @Size(min = 8)
    private String password;

    /** First name. */
    @Schema(description = "First name")
    @NotBlank
    private String firstName;

    /** Last name. */
    @Schema(description = "Last name")
    @NotBlank
    private String lastName;

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
}
