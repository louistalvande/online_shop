package com.shop.account.controller.impl;

import com.shop.account.controller.ProfileController;
import com.shop.account.dto.ProfileResponse;
import com.shop.account.dto.UpdateProfileRequest;
import com.shop.account.dto.VendorBannerUploadResponse;
import com.shop.account.dto.VendorLogoUploadResponse;
import com.shop.account.service.AccountService;
import com.shop.account.service.VendorBannerUploadService;
import com.shop.account.service.VendorLogoUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

/** Default implementation of {@link ProfileController}. */
@RestController
public class ProfileControllerImpl implements ProfileController {

    private final AccountService accountService;
    private final VendorLogoUploadService vendorLogoUploadService;
    private final VendorBannerUploadService vendorBannerUploadService;

    /**
     * @param accountService            the account business-logic service
     * @param vendorLogoUploadService   the logo image storage service
     * @param vendorBannerUploadService the banner image storage service
     */
    public ProfileControllerImpl(AccountService accountService,
                                  VendorLogoUploadService vendorLogoUploadService,
                                  VendorBannerUploadService vendorBannerUploadService) {
        this.accountService             = accountService;
        this.vendorLogoUploadService    = vendorLogoUploadService;
        this.vendorBannerUploadService  = vendorBannerUploadService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProfileResponse> getProfile(Principal principal) {
        return ResponseEntity.ok(accountService.getProfile(principal.getName()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProfileResponse> updateProfile(Principal principal,
                                                          UpdateProfileRequest request) {
        return ResponseEntity.ok(accountService.updateProfile(principal.getName(), request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<VendorLogoUploadResponse> uploadLogo(Principal principal, MultipartFile file) {
        String logoUrl = vendorLogoUploadService.store(file);
        return ResponseEntity.ok(new VendorLogoUploadResponse(logoUrl));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> deleteLogo(Principal principal) {
        vendorLogoUploadService.delete();
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<VendorBannerUploadResponse> uploadBanner(Principal principal, MultipartFile file) {
        String bannerUrl = vendorBannerUploadService.store(file);
        return ResponseEntity.ok(new VendorBannerUploadResponse(bannerUrl));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> deleteBanner(Principal principal) {
        vendorBannerUploadService.delete();
        return ResponseEntity.noContent().build();
    }
}
