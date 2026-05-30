package com.shop.catalog.controller;

import com.shop.catalog.dto.StockSubscriptionRequest;
import com.shop.catalog.dto.StockSubscriptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** Buyer endpoints for back-in-stock alert subscriptions (US-SHP-03 / FS-B14). */
@Tag(name = "Buyer — Stock subscriptions", description = "Back-in-stock email alert subscriptions")
@RequestMapping("/api/profile/stock-subscriptions")
public interface StockSubscriptionController {

    /**
     * Subscribes the authenticated buyer to a back-in-stock alert.
     *
     * @param principal the authenticated buyer
     * @param request   the subscription request containing the product ID
     * @return HTTP 201 with the created subscription
     */
    @Operation(summary = "Subscribe to a back-in-stock alert for an out-of-stock product")
    @ApiResponse(responseCode = "201", description = "Subscription created")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "409", description = "Already subscribed or product is in stock")
    @PostMapping
    ResponseEntity<StockSubscriptionResponse> subscribe(
            Principal principal,
            @Valid @RequestBody StockSubscriptionRequest request);

    /**
     * Cancels the buyer's active subscription for a product.
     *
     * @param principal the authenticated buyer
     * @param productId the UUID of the product to unsubscribe from
     * @return HTTP 204 No Content
     */
    @Operation(summary = "Cancel a back-in-stock subscription")
    @ApiResponse(responseCode = "204", description = "Subscription cancelled")
    @ApiResponse(responseCode = "404", description = "Subscription not found")
    @DeleteMapping("/{productId}")
    ResponseEntity<Void> unsubscribe(Principal principal, @PathVariable UUID productId);

    /**
     * Lists the authenticated buyer's active (pending) subscriptions.
     *
     * @param principal the authenticated buyer
     * @return HTTP 200 with the list of subscriptions
     */
    @Operation(summary = "List the buyer's active back-in-stock subscriptions")
    @ApiResponse(responseCode = "200", description = "List returned")
    @GetMapping
    ResponseEntity<List<StockSubscriptionResponse>> listSubscriptions(Principal principal);
}
