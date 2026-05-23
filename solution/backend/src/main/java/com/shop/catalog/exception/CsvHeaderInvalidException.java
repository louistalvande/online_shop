package com.shop.catalog.exception;

/** Thrown when the CSV file header does not match the expected format (US-CAT-06). */
public class CsvHeaderInvalidException extends RuntimeException {

    /** Constructs the exception with a fixed message. */
    public CsvHeaderInvalidException() {
        super("Invalid CSV header");
    }
}
