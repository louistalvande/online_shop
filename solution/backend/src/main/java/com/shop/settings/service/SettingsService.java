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

    /**
     * Returns the current shop accent colour (FS-V16).
     *
     * @return CSS hex colour string, defaults to {@code #4e8b82}
     */
    String getAccentColor();

    /**
     * Updates the shop accent colour (FS-V16).
     *
     * @param accentColor the new CSS hex colour (e.g. {@code #4e8b82})
     */
    void setAccentColor(String accentColor);

    /**
     * Returns the current shop background colour (FS-V16).
     *
     * @return CSS hex colour string, defaults to {@code #f2f6f5}
     */
    String getBgColor();

    /**
     * Updates the shop background colour (FS-V16).
     *
     * @param bgColor the new CSS hex colour (e.g. {@code #f2f6f5})
     */
    void setBgColor(String bgColor);

    /**
     * Returns the shop name displayed in the buyer-facing header.
     *
     * @return the shop name; defaults to {@code "Catalogue de dessins"} if absent
     */
    String getShopName();

    /**
     * Updates the shop name displayed in the buyer-facing header.
     *
     * @param shopName the new shop name
     */
    void setShopName(String shopName);
}
