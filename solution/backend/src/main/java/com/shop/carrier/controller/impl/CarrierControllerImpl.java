package com.shop.carrier.controller.impl;

import com.shop.carrier.controller.CarrierController;
import com.shop.carrier.dto.CarrierResponse;
import com.shop.carrier.dto.CreateCarrierRequest;
import com.shop.carrier.service.CarrierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

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
}
