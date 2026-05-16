package com.shop.carrier.dto;

import com.shop.carrier.entity.Carrier;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Carrier data returned by the API. */
public class CarrierResponse {

    @Schema(description = "Carrier UUID")
    private UUID id;

    @Schema(description = "Carrier display name")
    private String name;

    @Schema(description = "Parcel tracking URL")
    private String trackingUrl;

    @Schema(description = "Whether the carrier is currently active")
    private boolean active;

    @Schema(description = "ISO 3166-1 alpha-2 codes of supported Eurozone countries")
    private List<String> supportedCountries;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    /**
     * Builds a {@link CarrierResponse} from a {@link Carrier} entity.
     *
     * @param c the carrier entity
     * @return the corresponding DTO
     */
    public static CarrierResponse from(Carrier c) {
        CarrierResponse r = new CarrierResponse();
        r.id = c.getId();
        r.name = c.getName();
        r.trackingUrl = c.getTrackingUrl();
        r.active = c.isActive();
        r.supportedCountries = c.getSupportedCountries();
        r.createdAt = c.getCreatedAt();
        return r;
    }

    /** @return carrier UUID */
    public UUID getId() { return id; }

    /** @return carrier display name */
    public String getName() { return name; }

    /** @return parcel tracking URL */
    public String getTrackingUrl() { return trackingUrl; }

    /** @return {@code true} if active */
    public boolean isActive() { return active; }

    /** @return list of ISO 3166-1 alpha-2 codes */
    public List<String> getSupportedCountries() { return supportedCountries; }

    /** @return creation date-time */
    public LocalDateTime getCreatedAt() { return createdAt; }
}
