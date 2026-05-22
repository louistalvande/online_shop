package com.shop.account.service;

import com.shop.account.dto.DeliveryAddressRequest;
import com.shop.account.dto.DeliveryAddressResponse;

import java.util.List;
import java.util.UUID;

/** Business operations for the buyer's delivery address book (US-PRF-03). */
public interface DeliveryAddressService {

    /**
     * Returns all non-deleted delivery addresses for the authenticated buyer.
     *
     * @param buyerEmail the buyer's email (JWT subject)
     * @return list of active addresses, ordered by creation date
     */
    List<DeliveryAddressResponse> listAddresses(String buyerEmail);

    /**
     * Creates a new delivery address for the authenticated buyer.
     * If {@code request.isMakeDefault()} is true the new address becomes the default and the
     * previous default is cleared.  If this is the buyer's first address it is automatically
     * set as default regardless.
     *
     * @param buyerEmail the buyer's email
     * @param request    the address payload
     * @return the created address
     */
    DeliveryAddressResponse createAddress(String buyerEmail, DeliveryAddressRequest request);

    /**
     * Updates all fields of an existing delivery address.
     *
     * @param buyerEmail the buyer's email
     * @param addressId  the UUID of the address to update
     * @param request    the new values
     * @return the updated address
     * @throws com.shop.account.exception.DeliveryAddressNotFoundException if the address does not exist or belong to the buyer
     */
    DeliveryAddressResponse updateAddress(String buyerEmail, UUID addressId, DeliveryAddressRequest request);

    /**
     * Soft-deletes a delivery address.  Blocked if it is the last active address of the buyer.
     *
     * @param buyerEmail the buyer's email
     * @param addressId  the UUID of the address to delete
     * @throws com.shop.account.exception.DeliveryAddressNotFoundException if not found
     * @throws com.shop.account.exception.LastActiveAddressException if it is the last active address
     */
    void deleteAddress(String buyerEmail, UUID addressId);

    /**
     * Sets the given address as the buyer's default, clearing the previous default.
     *
     * @param buyerEmail the buyer's email
     * @param addressId  the UUID of the address to set as default
     * @return the updated address
     * @throws com.shop.account.exception.DeliveryAddressNotFoundException if not found
     */
    DeliveryAddressResponse setDefault(String buyerEmail, UUID addressId);
}
