package com.shop.announcement.dto;

import com.shop.announcement.entity.ImageOrientation;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response returned after a successful image upload (US-ANN-01). */
public class ImageUploadResponse {

    @Schema(description = "Public URL to the stored image")
    private String imageUrl;

    @Schema(description = "Auto-detected orientation: PORTRAIT if height >= width, LANDSCAPE if width > height")
    private ImageOrientation imageOrientation;

    /**
     * Constructs the response with a URL and the computed orientation.
     *
     * @param imageUrl         public URL of the stored image
     * @param imageOrientation auto-detected orientation
     */
    public ImageUploadResponse(String imageUrl, ImageOrientation imageOrientation) {
        this.imageUrl = imageUrl;
        this.imageOrientation = imageOrientation;
    }

    /** @return the public URL to the stored image */
    public String getImageUrl() { return imageUrl; }

    /** @return the auto-detected image orientation */
    public ImageOrientation getImageOrientation() { return imageOrientation; }
}
