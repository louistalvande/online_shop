package com.shop.settings.exception;

/** Thrown when a required platform setting is missing from the database. */
public class SettingNotFoundException extends RuntimeException {

    /**
     * Creates the exception for the given setting key.
     *
     * @param key the missing setting key
     */
    public SettingNotFoundException(String key) {
        super("Platform setting not found: " + key);
    }
}
