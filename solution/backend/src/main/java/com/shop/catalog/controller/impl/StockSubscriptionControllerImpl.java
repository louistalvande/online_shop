package com.shop.catalog.controller.impl;

import com.shop.catalog.controller.StockSubscriptionController;
import com.shop.catalog.dto.StockSubscriptionRequest;
import com.shop.catalog.dto.StockSubscriptionResponse;
import com.shop.catalog.service.StockSubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** {@link StockSubscriptionController} implementation. */
@RestController
public class StockSubscriptionControllerImpl implements StockSubscriptionController {

    private final StockSubscriptionService subscriptionService;

    /**
     * @param subscriptionService the subscription business logic layer
     */
    public StockSubscriptionControllerImpl(StockSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<StockSubscriptionResponse> subscribe(
            Principal principal, StockSubscriptionRequest request) {
        StockSubscriptionResponse created = subscriptionService.subscribe(
                principal.getName(), request.getProductId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{productId}")
                .buildAndExpand(created.getProductId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> unsubscribe(Principal principal, UUID productId) {
        subscriptionService.unsubscribe(principal.getName(), productId);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<StockSubscriptionResponse>> listSubscriptions(Principal principal) {
        return ResponseEntity.ok(subscriptionService.listSubscriptions(principal.getName()));
    }
}
