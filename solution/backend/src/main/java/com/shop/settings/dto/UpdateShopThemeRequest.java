package com.shop.settings.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

/** PATCH payload for vendor shop theme update (FS-V16). Fields are individually optional — only non-null values are applied. */
public class UpdateShopThemeRequest {

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Must be a 6-digit CSS hex colour (e.g. #4e8b82)")
    @Schema(description = "New shop accent colour — 6-digit CSS hex (e.g. #4e8b82); null = no change")
    private String accentColor;

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Must be a 6-digit CSS hex colour (e.g. #ffffff)")
    @Schema(description = "New shop background colour — 6-digit CSS hex (e.g. #f2f6f5); null = no change")
    private String bgColor;

    /** @return the new accent colour, or {@code null} if not being updated */
    public String getAccentColor() { return accentColor; }

    /** @param accentColor the new accent colour */
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; }

    /** @return the new background colour, or {@code null} if not being updated */
    public String getBgColor() { return bgColor; }

    /** @param bgColor the new background colour */
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }
}
