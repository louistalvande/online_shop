package com.shop.settings.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Request to enable or disable maintenance mode (US-ADM-10). */
public class SetMaintenanceModeRequest {

    @NotNull
    @Schema(description = "Set to true to enable maintenance mode, false to disable it", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean active;

    /** Returns the requested maintenance mode state. */
    public Boolean getActive() {
        return active;
    }

    /** Sets the requested maintenance mode state. */
    public void setActive(Boolean active) {
        this.active = active;
    }
}
