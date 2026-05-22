package com.shop.account.controller;

import com.shop.account.dto.DeliveryAddressRequest;
import com.shop.account.dto.DeliveryAddressResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** CRUD endpoints for the buyer's delivery address book (US-PRF-03). */
@Tag(name = "Delivery Addresses", description = "Buyer delivery address book (US-PRF-03)")
@RequestMapping("/api/profile/addresses")
public interface DeliveryAddressController {

    /**
     * Returns all active delivery addresses for the authenticated buyer.
     *
     * @param principal the JWT principal
     * @return 200 with the list of addresses
     */
    @Operation(summary = "List buyer delivery addresses (US-PRF-03)")
    @ApiResponse(responseCode = "200", description = "List returned")
    @GetMapping
    ResponseEntity<List<DeliveryAddressResponse>> listAddresses(Principal principal);

    /**
     * Creates a new delivery address.
     *
     * @param principal the JWT principal
     * @param request   the address payload
     * @return 201 with the created address
     */
    @Operation(summary = "Create a delivery address (US-PRF-03)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Address created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "422", description = "Country not in Eurozone")
    })
    @PostMapping
    ResponseEntity<DeliveryAddressResponse> createAddress(Principal principal,
                                                          @Valid @RequestBody DeliveryAddressRequest request);

    /**
     * Updates all fields of an existing delivery address.
     *
     * @param principal the JWT principal
     * @param addressId the UUID of the address to update
     * @param request   the new values
     * @return 200 with the updated address
     */
    @Operation(summary = "Update a delivery address (US-PRF-03)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Address updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PutMapping("/{addressId}")
    ResponseEntity<DeliveryAddressResponse> updateAddress(Principal principal,
                                                          @PathVariable UUID addressId,
                                                          @Valid @RequestBody DeliveryAddressRequest request);

    /**
     * Soft-deletes a delivery address.  Returns 409 if it is the last active address.
     *
     * @param principal the JWT principal
     * @param addressId the UUID of the address to delete
     * @return 204 on success
     */
    @Operation(summary = "Delete a delivery address (US-PRF-03)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Address deleted"),
        @ApiResponse(responseCode = "404", description = "Address not found"),
        @ApiResponse(responseCode = "409", description = "Cannot delete the last active address")
    })
    @DeleteMapping("/{addressId}")
    ResponseEntity<Void> deleteAddress(Principal principal, @PathVariable UUID addressId);

    /**
     * Sets the given address as the buyer's default.
     *
     * @param principal the JWT principal
     * @param addressId the UUID of the address to set as default
     * @return 200 with the updated address
     */
    @Operation(summary = "Set an address as default (US-PRF-03)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Default updated"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PatchMapping("/{addressId}/set-default")
    ResponseEntity<DeliveryAddressResponse> setDefault(Principal principal, @PathVariable UUID addressId);
}
