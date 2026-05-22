package com.shop.settings.controller;

import com.shop.settings.dto.MaintenanceStatusResponse;
import com.shop.settings.dto.SetMaintenanceModeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** Admin and public endpoints for platform maintenance mode (US-ADM-10). */
@Tag(name = "Admin — Settings", description = "Platform maintenance mode management")
@RequestMapping
public interface SettingsController {

    /**
     * Returns the current maintenance mode status (public endpoint, no authentication required).
     *
     * @return maintenance mode status with HTTP 200
     */
    @Operation(summary = "Get current maintenance mode status (public)")
    @ApiResponse(responseCode = "200", description = "Status returned")
    @GetMapping("/api/public/maintenance")
    ResponseEntity<MaintenanceStatusResponse> getMaintenanceStatus();

    /**
     * Returns the current maintenance mode status for admin callers.
     *
     * @return maintenance mode status with HTTP 200
     */
    @Operation(summary = "Get maintenance mode status (admin)")
    @ApiResponse(responseCode = "200", description = "Status returned")
    @GetMapping("/api/admin/settings/maintenance")
    ResponseEntity<MaintenanceStatusResponse> getMaintenanceStatusAdmin();

    /**
     * Enables or disables maintenance mode.
     *
     * @param request payload specifying the desired state
     * @return the updated maintenance mode status with HTTP 200
     */
    @Operation(summary = "Set maintenance mode")
    @ApiResponse(responseCode = "200", description = "Maintenance mode updated")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @PatchMapping("/api/admin/settings/maintenance")
    ResponseEntity<MaintenanceStatusResponse> setMaintenanceMode(@Valid @RequestBody SetMaintenanceModeRequest request);
}
