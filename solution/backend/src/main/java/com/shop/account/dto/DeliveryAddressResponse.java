package com.shop.account.dto;

import com.shop.account.entity.DeliveryAddress;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/** Response DTO for a buyer's delivery address (US-PRF-03). */
public class DeliveryAddressResponse {

    @Schema(description = "Address UUID") private UUID id;
    @Schema(description = "Short label") private String label;
    @Schema(description = "Street address line") private String addressLine;
    @Schema(description = "City") private String city;
    @Schema(description = "Postal code") private String postalCode;
    @Schema(description = "ISO alpha-2 country code") private String countryCode;
    @Schema(description = "True if this is the buyer's default delivery address") private boolean isDefault;

    private DeliveryAddressResponse() {}

    /**
     * Builds a response from a {@link DeliveryAddress} entity.
     *
     * @param address the address entity
     * @return the populated response DTO
     */
    public static DeliveryAddressResponse from(DeliveryAddress address) {
        DeliveryAddressResponse r = new DeliveryAddressResponse();
        r.id = address.getId();
        r.label = address.getLabel();
        r.addressLine = address.getAddressLine();
        r.city = address.getCity();
        r.postalCode = address.getPostalCode();
        r.countryCode = address.getCountryCode();
        r.isDefault = address.isDefault();
        return r;
    }

    /** @return the address UUID */
    public UUID getId() { return id; }
    /** @return the label */
    public String getLabel() { return label; }
    /** @return the street address line */
    public String getAddressLine() { return addressLine; }
    /** @return the city */
    public String getCity() { return city; }
    /** @return the postal code */
    public String getPostalCode() { return postalCode; }
    /** @return the country code */
    public String getCountryCode() { return countryCode; }
    /** @return true if this is the default address */
    public boolean isDefault() { return isDefault; }
}
