package com.shop.settings.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response carrying the current maintenance mode state (US-ADM-10). */
public class MaintenanceStatusResponse {

    @Schema(description = "True when the platform is in maintenance mode and non-admin access is blocked")
    private boolean active;

    /**
     * Creates a response with the given maintenance mode flag.
     *
     * @param active true if maintenance mode is currently enabled
     */
    public MaintenanceStatusResponse(boolean active) {
        this.active = active;
    }

    /** Returns whether maintenance mode is active. */
    public boolean isActive() {
        return active;
    }

    /** Sets the maintenance mode flag. */
    public void setActive(boolean active) {
        this.active = active;
    }
}
