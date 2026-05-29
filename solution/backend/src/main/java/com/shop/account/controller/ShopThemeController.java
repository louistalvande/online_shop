package com.shop.account.controller;

import com.shop.account.dto.ShopThemeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Public endpoint exposing the vendor-defined shop theme colours (FS-V16). */
@Tag(name = "Shop Theme", description = "Public shop theme — no authentication required (FS-V16)")
@RequestMapping("/api/public/theme")
public interface ShopThemeController {

    /**
     * Returns the current shop theme colours.
     * Called by the buyer portal on startup to apply vendor-defined CSS variables.
     *
     * @return 200 with the theme
     */
    @Operation(summary = "Get shop theme colours (FS-V16)")
    @ApiResponse(responseCode = "200", description = "Theme returned")
    @GetMapping
    ResponseEntity<ShopThemeResponse> getTheme();
}
