package com.shop.carrier.controller;

import com.shop.carrier.dto.CarrierResponse;
import com.shop.carrier.dto.CreateCarrierRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/** Admin endpoints for managing carriers (US-ADM-06). */
@Tag(name = "Admin — Carriers", description = "Carrier management for administrators")
@RequestMapping("/api/admin/carriers")
public interface CarrierController {

    /**
     * Creates a new carrier with active status.
     *
     * @param request the carrier creation payload
     * @return the created carrier with HTTP 201 and Location header
     */
    @Operation(summary = "Create a carrier")
    @ApiResponse(responseCode = "201", description = "Carrier created")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping
    ResponseEntity<CarrierResponse> createCarrier(@Valid @RequestBody CreateCarrierRequest request);

    /**
     * Returns all carriers.
     *
     * @return list of all carriers with HTTP 200
     */
    @Operation(summary = "List all carriers")
    @ApiResponse(responseCode = "200", description = "Carrier list returned")
    @GetMapping
    ResponseEntity<List<CarrierResponse>> listCarriers();
}
