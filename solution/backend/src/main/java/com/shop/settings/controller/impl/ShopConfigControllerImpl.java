package com.shop.settings.controller.impl;

import com.shop.account.dto.ShopThemeResponse;
import com.shop.account.service.VendorBannerUploadService;
import com.shop.account.service.VendorLogoUploadService;
import com.shop.settings.controller.ShopConfigController;
import com.shop.settings.dto.UpdateShopThemeRequest;
import com.shop.settings.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Default implementation of {@link ShopConfigController}. */
@RestController
public class ShopConfigControllerImpl implements ShopConfigController {

    private final SettingsService settingsService;
    private final VendorLogoUploadService vendorLogoUploadService;
    private final VendorBannerUploadService vendorBannerUploadService;

    /**
     * @param settingsService           global platform settings
     * @param vendorLogoUploadService   logo file presence check
     * @param vendorBannerUploadService banner file presence check
     */
    public ShopConfigControllerImpl(SettingsService settingsService,
                                     VendorLogoUploadService vendorLogoUploadService,
                                     VendorBannerUploadService vendorBannerUploadService) {
        this.settingsService            = settingsService;
        this.vendorLogoUploadService    = vendorLogoUploadService;
        this.vendorBannerUploadService  = vendorBannerUploadService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ShopThemeResponse> updateTheme(UpdateShopThemeRequest request) {
        if (request.getShopName()    != null && !request.getShopName().isBlank())
            settingsService.setShopName(request.getShopName().strip());
        if (request.getAccentColor() != null) settingsService.setAccentColor(request.getAccentColor());
        if (request.getBgColor()     != null) settingsService.setBgColor(request.getBgColor());
        if (request.getFooterNotice() != null) settingsService.setFooterNotice(request.getFooterNotice());
        return ResponseEntity.ok(new ShopThemeResponse(
                settingsService.getShopName(),
                settingsService.getAccentColor(),
                settingsService.getBgColor(),
                vendorLogoUploadService.getPublicLogoUrl(),
                vendorBannerUploadService.getPublicBannerUrl(),
                settingsService.getFooterNotice()
        ));
    }
}
