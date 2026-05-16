package com.shop.carrier.service.impl;

import com.shop.carrier.dto.CarrierResponse;
import com.shop.carrier.dto.CreateCarrierRequest;
import com.shop.carrier.dto.UpdateCarrierRequest;
import com.shop.carrier.entity.Carrier;
import com.shop.carrier.exception.CarrierNotFoundException;
import com.shop.carrier.repository.CarrierRepository;
import com.shop.carrier.service.CarrierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** {@link CarrierService} implementation. */
@Service
@Transactional
public class CarrierServiceImpl implements CarrierService {

    private final CarrierRepository carrierRepository;

    /**
     * Constructs the service with its required repository.
     *
     * @param carrierRepository the carrier JPA repository
     */
    public CarrierServiceImpl(CarrierRepository carrierRepository) {
        this.carrierRepository = carrierRepository;
    }

    /** {@inheritDoc} */
    @Override
    public CarrierResponse createCarrier(CreateCarrierRequest request) {
        Carrier carrier = new Carrier();
        carrier.setName(request.getName());
        carrier.setTrackingUrl(request.getTrackingUrl());
        carrier.setSupportedCountries(request.getSupportedCountries());
        carrier.setActive(true);
        carrier.setCreatedAt(LocalDateTime.now());
        return CarrierResponse.from(carrierRepository.save(carrier));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<CarrierResponse> listCarriers() {
        return carrierRepository.findAll().stream()
                .map(CarrierResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public CarrierResponse updateCarrier(UUID id, UpdateCarrierRequest request) {
        Carrier carrier = carrierRepository.findById(id)
                .orElseThrow(() -> new CarrierNotFoundException(id));
        carrier.setName(request.getName());
        carrier.setTrackingUrl(request.getTrackingUrl());
        carrier.getSupportedCountries().clear();
        carrier.getSupportedCountries().addAll(request.getSupportedCountries());
        return CarrierResponse.from(carrierRepository.save(carrier));
    }

    /** {@inheritDoc} */
    @Override
    public CarrierResponse deactivateCarrier(UUID id) {
        Carrier carrier = carrierRepository.findById(id)
                .orElseThrow(() -> new CarrierNotFoundException(id));
        carrier.setActive(false);
        return CarrierResponse.from(carrierRepository.save(carrier));
    }

    /** {@inheritDoc} */
    @Override
    public CarrierResponse activateCarrier(UUID id) {
        Carrier carrier = carrierRepository.findById(id)
                .orElseThrow(() -> new CarrierNotFoundException(id));
        carrier.setActive(true);
        return CarrierResponse.from(carrierRepository.save(carrier));
    }

    /** {@inheritDoc} */
    @Override
    public void deleteCarrier(UUID id) {
        if (!carrierRepository.existsById(id)) {
            throw new CarrierNotFoundException(id);
        }
        carrierRepository.deleteById(id);
    }
}
