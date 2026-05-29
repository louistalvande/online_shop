package com.shop.account.controller;

import com.shop.account.dto.ProfileResponse;
import com.shop.account.dto.UpdateProfileRequest;
import com.shop.account.dto.VendorBannerUploadResponse;
import com.shop.account.dto.VendorLogoUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

/** Self-service profile endpoints available to any authenticated user (US-PRF-01, US-PRF-02). */
@Tag(name = "Profile", description = "Self-service profile management (US-PRF-01, US-PRF-02)")
@RequestMapping("/api/me")
public interface ProfileController {

    /**
     * Returns the profile of the authenticated user.
     *
     * @param principal the JWT principal whose subject is the user's email
     * @return 200 with the profile
     */
    @Operation(summary = "Get own profile (US-PRF-01, US-PRF-02)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile returned"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    ResponseEntity<ProfileResponse> getProfile(Principal principal);

    /**
     * Updates the profile of the authenticated user.
     * Only non-null fields are applied. Password change requires all three password fields.
     *
     * @param principal the JWT principal whose subject is the user's email
     * @param request   the fields to update
     * @return 200 with the updated profile
     */
    @Operation(summary = "Update own profile (US-PRF-01, US-PRF-02)")
    @ApiResponses({
        @ApiResponse(responseCode = "200",  description = "Profile updated"),
        @ApiResponse(responseCode = "400",  description = "Validation error"),
        @ApiResponse(responseCode = "401",  description = "Not authenticated"),
        @ApiResponse(responseCode = "422",  description = "Wrong current password")
    })
    @PatchMapping
    ResponseEntity<ProfileResponse> updateProfile(Principal principal,
                                                   @Valid @RequestBody UpdateProfileRequest request);

    /**
     * Uploads a vendor branding logo and stores its public URL on the authenticated account (US-PRF-01).
     *
     * @param principal the JWT principal whose subject is the vendor's email
     * @param file      the logo image file (JPEG, PNG, or WebP)
     * @return 200 with the public URL of the uploaded logo
     */
    @Operation(summary = "Upload vendor branding logo (US-PRF-01)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logo uploaded and profile updated"),
        @ApiResponse(responseCode = "400", description = "Unsupported image type"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<VendorLogoUploadResponse> uploadLogo(Principal principal,
                                                         @RequestParam("file") MultipartFile file);

    /**
     * Deletes the vendor branding logo file (FS-V16).
     *
     * @param principal the JWT principal whose subject is the vendor's email
     * @return 204 No Content
     */
    @Operation(summary = "Delete vendor branding logo (FS-V16)")
    @ApiResponse(responseCode = "204", description = "Logo deleted")
    @DeleteMapping("/logo")
    ResponseEntity<Void> deleteLogo(Principal principal);

    /**
     * Uploads a vendor hero banner and stores it on the filesystem (FS-V16).
     *
     * @param principal the JWT principal whose subject is the vendor's email
     * @param file      the banner image file (JPEG, PNG, or WebP)
     * @return 200 with the public URL of the uploaded banner
     */
    @Operation(summary = "Upload vendor hero banner (FS-V16)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Banner uploaded"),
        @ApiResponse(responseCode = "400", description = "Unsupported image type"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping(value = "/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<VendorBannerUploadResponse> uploadBanner(Principal principal,
                                                             @RequestParam("file") MultipartFile file);

    /**
     * Deletes the vendor hero banner file (FS-V16).
     *
     * @param principal the JWT principal whose subject is the vendor's email
     * @return 204 No Content
     */
    @Operation(summary = "Delete vendor hero banner (FS-V16)")
    @ApiResponse(responseCode = "204", description = "Banner deleted")
    @DeleteMapping("/banner")
    ResponseEntity<Void> deleteBanner(Principal principal);
}
