package com.shop.account.exception;

/** Thrown when an uploaded vendor banner is not a supported image type (JPEG, PNG, WebP). */
public class UnsupportedBannerImageTypeException extends RuntimeException {

    /**
     * Constructs the exception for the given MIME type.
     *
     * @param contentType the unsupported MIME type, or {@code null} when none was detected
     */
    public UnsupportedBannerImageTypeException(String contentType) {
        super("Unsupported banner image type: " + contentType);
    }
}
