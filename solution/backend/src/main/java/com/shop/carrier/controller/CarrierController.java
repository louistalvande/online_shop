package com.shop.carrier.controller;

import com.shop.carrier.dto.CarrierResponse;
import com.shop.carrier.dto.CreateCarrierRequest;
import com.shop.carrier.dto.UpdateCarrierRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Admin endpoints for managing carriers (US-ADM-06 to US-ADM-09). */
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

    /**
     * Updates a carrier's name, tracking URL and supported countries (US-ADM-07).
     *
     * @param id      the carrier UUID
     * @param request the update payload
     * @return the updated carrier with HTTP 200
     */
    @Operation(summary = "Update a carrier")
    @ApiResponse(responseCode = "200", description = "Carrier updated")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Carrier not found")
    @PatchMapping("/{id}")
    ResponseEntity<CarrierResponse> updateCarrier(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateCarrierRequest request);

    /**
     * Deactivates a carrier so it is no longer available for new shipments (US-ADM-08).
     *
     * @param id the carrier UUID
     * @return the updated carrier with HTTP 200
     */
    @Operation(summary = "Deactivate a carrier")
    @ApiResponse(responseCode = "200", description = "Carrier deactivated")
    @ApiResponse(responseCode = "404", description = "Carrier not found")
    @PatchMapping("/{id}/deactivate")
    ResponseEntity<CarrierResponse> deactivateCarrier(@PathVariable UUID id);

    /**
     * Reactivates a previously deactivated carrier (US-ADM-08).
     *
     * @param id the carrier UUID
     * @return the updated carrier with HTTP 200
     */
    @Operation(summary = "Activate a carrier")
    @ApiResponse(responseCode = "200", description = "Carrier activated")
    @ApiResponse(responseCode = "404", description = "Carrier not found")
    @PatchMapping("/{id}/activate")
    ResponseEntity<CarrierResponse> activateCarrier(@PathVariable UUID id);

    /**
     * Permanently deletes a carrier (US-ADM-09).
     *
     * @param id the carrier UUID
     * @return HTTP 204 No Content
     */
    @Operation(summary = "Delete a carrier")
    @ApiResponse(responseCode = "204", description = "Carrier deleted")
    @ApiResponse(responseCode = "404", description = "Carrier not found")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteCarrier(@PathVariable UUID id);
}
