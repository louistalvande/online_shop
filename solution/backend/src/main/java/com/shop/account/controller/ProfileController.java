package com.shop.account.controller;

import com.shop.account.dto.ProfileResponse;
import com.shop.account.dto.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
