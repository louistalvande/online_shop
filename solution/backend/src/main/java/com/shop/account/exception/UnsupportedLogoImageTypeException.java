package com.shop.account.exception;

/** Thrown when an uploaded vendor logo is not a supported image type (JPEG, PNG, WebP). */
public class UnsupportedLogoImageTypeException extends RuntimeException {

    /**
     * Constructs the exception for the given MIME type.
     *
     * @param contentType the unsupported MIME type, or {@code null} when none was detected
     */
    public UnsupportedLogoImageTypeException(String contentType) {
        super("Unsupported logo image type: " + contentType);
    }
}
