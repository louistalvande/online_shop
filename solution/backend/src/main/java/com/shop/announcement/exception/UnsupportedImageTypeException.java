package com.shop.announcement.exception;

/** Thrown when an uploaded file is not a supported image type (JPEG, PNG, GIF, WebP). */
public class UnsupportedImageTypeException extends RuntimeException {

    /**
     * Constructs the exception for the given MIME type.
     *
     * @param contentType the unsupported MIME type
     */
    public UnsupportedImageTypeException(String contentType) {
        super("Unsupported image type: " + contentType);
    }
}
