package com.shop.claim.controller;

import com.shop.claim.dto.ClaimResponse;
import com.shop.claim.dto.CreateClaimRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** REST API for buyer claim operations (US-CLM-01). */
@Tag(name = "Claims", description = "Buyer claim lifecycle")
@RequestMapping("/api/orders/{orderId}/claims")
public interface ClaimController {

    /**
     * Opens a new claim for the specified order.
     *
     * @param orderId   the order UUID
     * @param request   claim reason and message
     * @param principal authenticated buyer
     * @param locale    buyer locale
     * @return 201 with the created claim
     */
    @Operation(summary = "Open a claim for an order (US-CLM-01)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Claim created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order not in a claimable state, or claim already open")
    })
    @PostMapping
    ResponseEntity<ClaimResponse> openClaim(
            @PathVariable UUID orderId,
            @Valid @RequestBody CreateClaimRequest request,
            Principal principal,
            Locale locale);

    /**
     * Returns all claims filed by the authenticated buyer for the specified order.
     *
     * @param orderId   the order UUID
     * @param principal authenticated buyer
     * @return 200 with the list of claims
     */
    @Operation(summary = "List claims for an order (US-CLM-01)")
    @ApiResponse(responseCode = "200", description = "Claims retrieved")
    @GetMapping
    ResponseEntity<List<ClaimResponse>> getOrderClaims(
            @PathVariable UUID orderId,
            Principal principal);
}
