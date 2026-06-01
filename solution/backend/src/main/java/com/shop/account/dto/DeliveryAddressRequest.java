package com.shop.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request payload to create or update a delivery address (US-PRF-03). */
public class DeliveryAddressRequest {

    @Schema(description = "Short label for this address, e.g. Home or Office")
    @NotBlank
    @Size(max = 100)
    private String label;

    @Schema(description = "Name of the parcel recipient — may differ from the buyer's account name")
    @NotBlank
    @Size(max = 100)
    private String recipientName;

    @Schema(description = "Street address line")
    @NotBlank
    @Size(max = 255)
    private String addressLine;

    @Schema(description = "City")
    @NotBlank
    @Size(max = 100)
    private String city;

    @Schema(description = "Postal code")
    @NotBlank
    @Size(max = 20)
    private String postalCode;

    @Schema(description = "ISO 3166-1 alpha-2 country code — must be a Eurozone country (CS-04)")
    @NotBlank
    @Pattern(regexp = "[A-Z]{2}", message = "{error.country.code.invalid}")
    private String countryCode;

    @Schema(description = "Set to true to make this address the default at checkout")
    private boolean makeDefault = false;

    /** @return the address label */
    public String getLabel() { return label; }
    /** @param label the address label */
    public void setLabel(String label) { this.label = label; }

    /** @return the recipient name */
    public String getRecipientName() { return recipientName; }
    /** @param recipientName the recipient name */
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    /** @return the street address line */
    public String getAddressLine() { return addressLine; }
    /** @param addressLine the street address line */
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }

    /** @return the city */
    public String getCity() { return city; }
    /** @param city the city */
    public void setCity(String city) { this.city = city; }

    /** @return the postal code */
    public String getPostalCode() { return postalCode; }
    /** @param postalCode the postal code */
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    /** @return the ISO alpha-2 country code */
    public String getCountryCode() { return countryCode; }
    /** @param countryCode the ISO alpha-2 country code */
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    /** @return true if this address should be set as the default */
    public boolean isMakeDefault() { return makeDefault; }
    /** @param makeDefault true to mark this address as the default */
    public void setMakeDefault(boolean makeDefault) { this.makeDefault = makeDefault; }
}
