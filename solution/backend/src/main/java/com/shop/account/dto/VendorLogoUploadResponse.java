package com.shop.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response returned after a successful vendor logo upload (US-PRF-01). */
public class VendorLogoUploadResponse {

    @Schema(description = "Public URL of the uploaded vendor logo")
    private final String logoUrl;

    /**
     * @param logoUrl public URL of the stored logo image
     */
    public VendorLogoUploadResponse(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    /** @return the public URL of the uploaded logo */
    public String getLogoUrl() { return logoUrl; }
}
