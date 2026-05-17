package com.shop.account.dto;

import com.shop.account.entity.AccountLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * PATCH payload for self-service profile update (US-PRF-01, US-PRF-02).
 * All fields are optional; only non-null values are applied.
 * Password change requires all three password fields to be present.
 */
public class UpdateProfileRequest {

    @Size(max = 100)
    @Schema(description = "Given name to set — omit to leave unchanged") private String firstName;

    @Size(max = 100)
    @Schema(description = "Family name to set — omit to leave unchanged") private String lastName;

    @Size(max = 20)
    @Schema(description = "Phone number to set — omit to leave unchanged") private String phone;

    @Size(max = 255)
    @Schema(description = "Street address line to set — omit to leave unchanged") private String addressLine;

    @Size(max = 100)
    @Schema(description = "City to set — omit to leave unchanged") private String city;

    @Size(max = 20)
    @Schema(description = "Postal code to set — omit to leave unchanged") private String postalCode;

    @Size(min = 2, max = 2)
    @Schema(description = "ISO 3166-1 alpha-2 country code (euro zone) — omit to leave unchanged") private String countryCode;

    @Schema(description = "Preferred notification language to set — omit to leave unchanged") private AccountLanguage language;

    @Schema(description = "Current password — required when changing password") private String currentPassword;

    @Size(min = 8)
    @Schema(description = "New password — required with currentPassword and confirmPassword") private String newPassword;

    @Schema(description = "New password confirmation — must match newPassword") private String confirmPassword;

    /** @return the given name, or {@code null} if unchanged */
    public String getFirstName() { return firstName; }

    /** @param firstName the given name to set */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** @return the family name, or {@code null} if unchanged */
    public String getLastName() { return lastName; }

    /** @param lastName the family name to set */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /** @return the phone number, or {@code null} if unchanged */
    public String getPhone() { return phone; }

    /** @param phone the phone number to set */
    public void setPhone(String phone) { this.phone = phone; }

    /** @return the street address line, or {@code null} if unchanged */
    public String getAddressLine() { return addressLine; }

    /** @param addressLine the street address line to set */
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }

    /** @return the city, or {@code null} if unchanged */
    public String getCity() { return city; }

    /** @param city the city to set */
    public void setCity(String city) { this.city = city; }

    /** @return the postal code, or {@code null} if unchanged */
    public String getPostalCode() { return postalCode; }

    /** @param postalCode the postal code to set */
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    /** @return the country code, or {@code null} if unchanged */
    public String getCountryCode() { return countryCode; }

    /** @param countryCode the country code to set */
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    /** @return the preferred notification language, or {@code null} if unchanged */
    public AccountLanguage getLanguage() { return language; }

    /** @param language the language to set */
    public void setLanguage(AccountLanguage language) { this.language = language; }

    /** @return the current password for verification, or {@code null} if not changing password */
    public String getCurrentPassword() { return currentPassword; }

    /** @param currentPassword the current password for verification */
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    /** @return the new password, or {@code null} if not changing password */
    public String getNewPassword() { return newPassword; }

    /** @param newPassword the new password to set */
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    /** @return the new password confirmation, or {@code null} if not changing password */
    public String getConfirmPassword() { return confirmPassword; }

    /** @param confirmPassword the new password confirmation */
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
