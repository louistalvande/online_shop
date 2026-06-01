package com.shop.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

/**
 * Vendor-facing endpoint to export the marketing mailing list as a CSV file (US-PRF-05 / RGPD-CONS-004).
 */
@Tag(name = "Marketing Consent", description = "Vendor mailing list export (US-PRF-05)")
@RequestMapping("/api/vendor/marketing-consent")
public interface MarketingConsentController {

    /**
     * Returns a CSV file containing email, first name and last name of every active buyer
     * who has opted in to marketing emails. The export is recorded in the audit log.
     *
     * @param principal the authenticated vendor principal
     * @return 200 with CSV attachment; 401 if not authenticated
     */
    @Operation(summary = "Export mailing list as CSV (US-PRF-05)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSV file returned"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping(value = "/export", produces = "text/csv")
    ResponseEntity<byte[]> exportMailingList(Principal principal);
}
