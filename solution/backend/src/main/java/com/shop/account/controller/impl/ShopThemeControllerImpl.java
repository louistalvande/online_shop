package com.shop.account.controller.impl;

import com.shop.account.controller.ShopThemeController;
import com.shop.account.dto.ShopThemeResponse;
import com.shop.account.service.VendorBannerUploadService;
import com.shop.account.service.VendorLogoUploadService;
import com.shop.settings.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Default implementation of {@link ShopThemeController}. */
@RestController
public class ShopThemeControllerImpl implements ShopThemeController {

    private final SettingsService settingsService;
    private final VendorLogoUploadService vendorLogoUploadService;
    private final VendorBannerUploadService vendorBannerUploadService;

    /**
     * @param settingsService           global platform settings (accent colour)
     * @param vendorLogoUploadService   logo file presence check
     * @param vendorBannerUploadService banner file presence check
     */
    public ShopThemeControllerImpl(SettingsService settingsService,
                                    VendorLogoUploadService vendorLogoUploadService,
                                    VendorBannerUploadService vendorBannerUploadService) {
        this.settingsService            = settingsService;
        this.vendorLogoUploadService    = vendorLogoUploadService;
        this.vendorBannerUploadService  = vendorBannerUploadService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ShopThemeResponse> getTheme() {
        return ResponseEntity.ok(new ShopThemeResponse(
                settingsService.getAccentColor(),
                vendorLogoUploadService.getPublicLogoUrl(),
                vendorBannerUploadService.getPublicBannerUrl()
        ));
    }
}
