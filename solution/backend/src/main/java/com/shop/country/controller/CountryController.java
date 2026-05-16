package com.shop.country.controller;

import com.shop.country.dto.CountryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/** Admin endpoint for Eurozone country reference data (CS-04). */
@Tag(name = "Admin — Countries", description = "Eurozone country reference list")
@RequestMapping("/api/admin/countries")
public interface CountryController {

    /**
     * Returns all Eurozone member countries.
     *
     * @return list of countries with HTTP 200
     */
    @Operation(summary = "List all Eurozone countries")
    @ApiResponse(responseCode = "200", description = "Country list returned")
    @GetMapping
    ResponseEntity<List<CountryResponse>> listCountries();
}
