package com.shop.common.controller;

import com.shop.carrier.dto.CarrierResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/** Public reference-data endpoints used by the buyer checkout flow. */
@Tag(name = "Public", description = "Reference data for checkout (no authentication required)")
@RequestMapping("/api")
public interface PublicController {

    /**
     * Returns active carriers that cover the given delivery country (US-ORD-02).
     *
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @return 200 with the list of matching carriers
     */
    @Operation(summary = "List active carriers available for a delivery country")
    @ApiResponse(responseCode = "200", description = "Carriers returned")
    @GetMapping("/carriers")
    ResponseEntity<List<CarrierResponse>> listCarriers(@RequestParam String countryCode);

    /**
     * Returns all Eurozone countries available for delivery (CS-04).
     *
     * @return 200 with list of country objects containing code and localised name
     */
    @Operation(summary = "List Eurozone countries available for delivery")
    @ApiResponse(responseCode = "200", description = "Country list returned")
    @GetMapping("/countries")
    ResponseEntity<List<Map<String, String>>> listCountries();
}
