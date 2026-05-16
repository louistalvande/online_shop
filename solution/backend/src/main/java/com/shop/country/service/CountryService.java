package com.shop.country.service;

import com.shop.country.dto.CountryResponse;

import java.util.List;

/** Business operations for Eurozone country reference data (CS-04). */
public interface CountryService {

    /**
     * Returns all Eurozone member countries.
     *
     * @return list of country DTOs
     */
    List<CountryResponse> listCountries();
}
