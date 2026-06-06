package com.shop.settings.controller.impl;

import com.shop.settings.controller.SettingsController;
import com.shop.settings.dto.LegalPageResponse;
import com.shop.settings.dto.MaintenanceStatusResponse;
import com.shop.settings.dto.SetMaintenanceModeRequest;
import com.shop.settings.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** {@link SettingsController} implementation. */
@RestController
public class SettingsControllerImpl implements SettingsController {

    private final SettingsService settingsService;

    /**
     * Constructs the controller with the settings service.
     *
     * @param settingsService the platform settings service
     */
    public SettingsControllerImpl(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MaintenanceStatusResponse> getMaintenanceStatus() {
        return ResponseEntity.ok(settingsService.getMaintenanceStatus());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MaintenanceStatusResponse> getMaintenanceStatusAdmin() {
        return ResponseEntity.ok(settingsService.getMaintenanceStatus());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MaintenanceStatusResponse> setMaintenanceMode(SetMaintenanceModeRequest request) {
        return ResponseEntity.ok(settingsService.setMaintenanceMode(request.getActive()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<LegalPageResponse> getLegalPage(String key) {
        String content = settingsService.getLegalPage(key);
        return ResponseEntity.ok(new LegalPageResponse(key, content));
    }
}
