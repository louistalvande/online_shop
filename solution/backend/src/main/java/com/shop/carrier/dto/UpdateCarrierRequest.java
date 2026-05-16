package com.shop.carrier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Payload for updating an existing carrier (US-ADM-07). */
public class UpdateCarrierRequest {

    @Schema(description = "Carrier display name")
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(description = "Parcel tracking URL")
    @NotBlank
    @Size(max = 500)
    private String trackingUrl;

    @Schema(description = "ISO 3166-1 alpha-2 codes of supported Eurozone countries (CS-04)")
    @NotEmpty
    private List<String> supportedCountries;

    /** @return carrier name */
    public String getName() { return name; }

    /** @param name carrier name */
    public void setName(String name) { this.name = name; }

    /** @return tracking URL */
    public String getTrackingUrl() { return trackingUrl; }

    /** @param trackingUrl tracking URL */
    public void setTrackingUrl(String trackingUrl) { this.trackingUrl = trackingUrl; }

    /** @return list of supported Eurozone country codes */
    public List<String> getSupportedCountries() { return supportedCountries; }

    /** @param supportedCountries list of ISO 3166-1 alpha-2 codes */
    public void setSupportedCountries(List<String> supportedCountries) {
        this.supportedCountries = supportedCountries;
    }
}
