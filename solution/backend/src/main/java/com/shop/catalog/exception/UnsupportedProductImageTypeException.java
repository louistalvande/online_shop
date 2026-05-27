package com.shop.catalog.exception;

/** Thrown when an uploaded file is not a supported image type for product photos (US-CAT-09). */
public class UnsupportedProductImageTypeException extends RuntimeException {

    /**
     * Constructs the exception for the given unsupported content type.
     *
     * @param contentType the unsupported MIME type, or {@code null} if absent
     */
    public UnsupportedProductImageTypeException(String contentType) {
        super("Unsupported product image type: " + contentType);
    }
}
