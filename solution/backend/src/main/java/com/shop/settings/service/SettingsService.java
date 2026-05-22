package com.shop.settings.service;

import com.shop.settings.dto.MaintenanceStatusResponse;

/** Business operations for platform settings (US-ADM-10). */
public interface SettingsService {

    /**
     * Returns the current maintenance mode status.
     *
     * @return DTO with {@code active = true} when maintenance mode is enabled
     */
    MaintenanceStatusResponse getMaintenanceStatus();

    /**
     * Enables or disables maintenance mode.
     *
     * @param active {@code true} to enable maintenance mode, {@code false} to disable it
     * @return the updated maintenance mode status
     */
    MaintenanceStatusResponse setMaintenanceMode(boolean active);

    /**
     * Returns whether maintenance mode is currently active.
     * Intended for lightweight checks in filters.
     *
     * @return {@code true} when maintenance mode is enabled
     */
    boolean isMaintenanceActive();
}
