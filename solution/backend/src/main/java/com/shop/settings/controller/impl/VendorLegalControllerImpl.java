package com.shop.settings.controller.impl;

import com.shop.settings.controller.VendorLegalController;
import com.shop.settings.dto.LegalPageResponse;
import com.shop.settings.dto.UpdateLegalPageRequest;
import com.shop.settings.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Default implementation of {@link VendorLegalController}. */
@RestController
public class VendorLegalControllerImpl implements VendorLegalController {

    private final SettingsService settingsService;

    /**
     * @param settingsService the platform settings service
     */
    public VendorLegalControllerImpl(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<LegalPageResponse> updateLegalPage(String key, UpdateLegalPageRequest request) {
        settingsService.setLegalPage(key, request.getContent());
        return ResponseEntity.ok(new LegalPageResponse(key, request.getContent()));
    }
}
