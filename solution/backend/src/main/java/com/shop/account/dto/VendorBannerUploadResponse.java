package com.shop.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response returned after a successful vendor banner upload (FS-V16). */
public class VendorBannerUploadResponse {

    @Schema(description = "Public URL of the uploaded vendor hero banner")
    private final String bannerUrl;

    /**
     * @param bannerUrl public URL of the stored banner image
     */
    public VendorBannerUploadResponse(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    /** @return the public URL of the uploaded banner */
    public String getBannerUrl() { return bannerUrl; }
}
