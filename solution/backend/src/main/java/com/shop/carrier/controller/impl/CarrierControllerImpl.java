package com.shop.carrier.controller.impl;

import com.shop.carrier.controller.CarrierController;
import com.shop.carrier.dto.CarrierResponse;
import com.shop.carrier.dto.CreateCarrierRequest;
import com.shop.carrier.dto.UpdateCarrierRequest;
import com.shop.carrier.service.CarrierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/** {@link CarrierController} implementation. */
@RestController
public class CarrierControllerImpl implements CarrierController {

    private final CarrierService carrierService;

    /**
     * Constructs the controller with its required service.
     *
     * @param carrierService the carrier service
     */
    public CarrierControllerImpl(CarrierService carrierService) {
        this.carrierService = carrierService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CarrierResponse> createCarrier(CreateCarrierRequest request) {
        CarrierResponse created = carrierService.createCarrier(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<CarrierResponse>> listCarriers() {
        return ResponseEntity.ok(carrierService.listCarriers());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CarrierResponse> updateCarrier(UUID id, UpdateCarrierRequest request) {
        return ResponseEntity.ok(carrierService.updateCarrier(id, request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CarrierResponse> deactivateCarrier(UUID id) {
        return ResponseEntity.ok(carrierService.deactivateCarrier(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CarrierResponse> activateCarrier(UUID id) {
        return ResponseEntity.ok(carrierService.activateCarrier(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> deleteCarrier(UUID id) {
        carrierService.deleteCarrier(id);
        return ResponseEntity.noContent().build();
    }
}
