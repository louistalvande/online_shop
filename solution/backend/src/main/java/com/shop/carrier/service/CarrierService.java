package com.shop.carrier.service;

import com.shop.carrier.dto.CarrierResponse;
import com.shop.carrier.dto.CreateCarrierRequest;

import java.util.List;

/** Business operations for carrier management (US-ADM-06). */
public interface CarrierService {

    /**
     * Creates a new carrier with {@code active = true}.
     *
     * @param request the creation payload
     * @return the created carrier DTO
     */
    CarrierResponse createCarrier(CreateCarrierRequest request);

    /**
     * Returns all carriers regardless of status.
     *
     * @return list of all carrier DTOs
     */
    List<CarrierResponse> listCarriers();
}
