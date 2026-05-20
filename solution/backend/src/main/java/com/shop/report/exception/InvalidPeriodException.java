package com.shop.report.exception;

/** Thrown when a sales report period parameter is not in YYYY-MM format (US-RPT-01). */
public class InvalidPeriodException extends RuntimeException {

    /**
     * @param period the invalid period string that was provided
     */
    public InvalidPeriodException(String period) {
        super("Invalid period: " + period + ". Expected format: YYYY-MM.");
    }
}
