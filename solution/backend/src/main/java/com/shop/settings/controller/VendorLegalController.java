package com.shop.settings.controller;

import com.shop.settings.dto.LegalPageResponse;
import com.shop.settings.dto.UpdateLegalPageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** Vendor-only endpoint for managing legal page content. */
@Tag(name = "Vendor — Legal Pages", description = "Legal page content management (vendor only)")
@RequestMapping("/api/vendor/legal")
public interface VendorLegalController {

    /**
     * Updates the content of a legal page.
     *
     * @param key     the legal page key (e.g. {@code legal_cgv})
     * @param request the new content
     * @return 200 with the updated page, or 404 if the key is unknown
     */
    @Operation(summary = "Update legal page content (vendor)")
    @ApiResponse(responseCode = "200", description = "Content updated")
    @ApiResponse(responseCode = "404", description = "Unknown legal page key")
    @PatchMapping("/{key}")
    ResponseEntity<LegalPageResponse> updateLegalPage(@PathVariable String key,
                                                       @Valid @RequestBody UpdateLegalPageRequest request);
}
