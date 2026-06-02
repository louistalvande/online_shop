package com.shop.settings.controller;

import com.shop.account.dto.ShopThemeResponse;
import com.shop.settings.dto.UpdateShopThemeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** Vendor-only endpoint to update global shop theme configuration (FS-V16). */
@Tag(name = "Vendor — Shop Config", description = "Global shop theme management (FS-V16)")
@RequestMapping("/api/vendor/shop/theme")
public interface ShopConfigController {

    /**
     * Updates shop theme colours (accent and/or background) and returns the full updated theme.
     * Fields are individually optional — only non-null values are applied.
     *
     * @param request the new theme values (at least one field should be non-null)
     * @return 200 with the updated shop theme
     */
    @Operation(summary = "Update shop theme colours (FS-V16)")
    @ApiResponse(responseCode = "200", description = "Theme updated")
    @ApiResponse(responseCode = "400", description = "Invalid hex colour")
    @PatchMapping
    ResponseEntity<ShopThemeResponse> updateTheme(@Valid @RequestBody UpdateShopThemeRequest request);
}
