package com.shop.settings.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** PATCH payload for vendor shop theme update (FS-V16). */
public class UpdateShopThemeRequest {

    @NotBlank
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Must be a 6-digit CSS hex colour (e.g. #4e8b82)")
    @Schema(description = "New shop accent colour — 6-digit CSS hex (e.g. #4e8b82)")
    private String accentColor;

    /** @return the new accent colour */
    public String getAccentColor() { return accentColor; }

    /** @param accentColor the new accent colour */
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; }
}
