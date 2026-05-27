package com.shop.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response returned after a successful product image upload (US-CAT-09). */
public class ProductImageUploadResponse {

    @Schema(description = "Public URL to the stored product image, ready to be saved in the product's photo list")
    private final String imageUrl;

    /**
     * Constructs the response with the public URL of the stored image.
     *
     * @param imageUrl public URL of the stored image
     */
    public ProductImageUploadResponse(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /** @return the public URL to the stored product image */
    public String getImageUrl() { return imageUrl; }
}
