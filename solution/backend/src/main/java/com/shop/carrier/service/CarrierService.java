package com.shop.carrier.service;

import com.shop.carrier.dto.CarrierResponse;
import com.shop.carrier.dto.CreateCarrierRequest;
import com.shop.carrier.dto.UpdateCarrierRequest;

import java.util.List;
import java.util.UUID;

/** Business operations for carrier management (US-ADM-06 to US-ADM-09). */
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

    /**
     * Updates a carrier's name, tracking URL and supported countries (US-ADM-07).
     *
     * @param id      the carrier's UUID
     * @param request the update payload
     * @return the updated carrier DTO
     * @throws com.shop.carrier.exception.CarrierNotFoundException when no carrier exists for the given id
     */
    CarrierResponse updateCarrier(UUID id, UpdateCarrierRequest request);

    /**
     * Deactivates a carrier so it is no longer available for new shipments (US-ADM-08).
     *
     * @param id the carrier's UUID
     * @return the updated carrier DTO with {@code active = false}
     * @throws com.shop.carrier.exception.CarrierNotFoundException when no carrier exists for the given id
     */
    CarrierResponse deactivateCarrier(UUID id);

    /**
     * Reactivates a previously deactivated carrier (US-ADM-08).
     *
     * @param id the carrier's UUID
     * @return the updated carrier DTO with {@code active = true}
     * @throws com.shop.carrier.exception.CarrierNotFoundException when no carrier exists for the given id
     */
    CarrierResponse activateCarrier(UUID id);

    /**
     * Permanently deletes a carrier (US-ADM-09).
     * Blocked at database level once shipments referencing this carrier exist.
     *
     * @param id the carrier's UUID
     * @throws com.shop.carrier.exception.CarrierNotFoundException when no carrier exists for the given id
     */
    void deleteCarrier(UUID id);
}
