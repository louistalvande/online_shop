package com.shop.account.controller.impl;

import com.shop.account.controller.DeliveryAddressController;
import com.shop.account.dto.DeliveryAddressRequest;
import com.shop.account.dto.DeliveryAddressResponse;
import com.shop.account.service.DeliveryAddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** REST implementation of {@link DeliveryAddressController}. */
@RestController
public class DeliveryAddressControllerImpl implements DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    /**
     * Constructs the controller with its required service.
     *
     * @param deliveryAddressService the address book service
     */
    public DeliveryAddressControllerImpl(DeliveryAddressService deliveryAddressService) {
        this.deliveryAddressService = deliveryAddressService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<DeliveryAddressResponse>> listAddresses(Principal principal) {
        return ResponseEntity.ok(deliveryAddressService.listAddresses(principal.getName()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DeliveryAddressResponse> createAddress(Principal principal,
                                                                  DeliveryAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryAddressService.createAddress(principal.getName(), request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DeliveryAddressResponse> updateAddress(Principal principal,
                                                                  UUID addressId,
                                                                  DeliveryAddressRequest request) {
        return ResponseEntity.ok(deliveryAddressService.updateAddress(principal.getName(), addressId, request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> deleteAddress(Principal principal, UUID addressId) {
        deliveryAddressService.deleteAddress(principal.getName(), addressId);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DeliveryAddressResponse> setDefault(Principal principal, UUID addressId) {
        return ResponseEntity.ok(deliveryAddressService.setDefault(principal.getName(), addressId));
    }
}
