package com.shop.country.controller.impl;

import com.shop.country.controller.CountryController;
import com.shop.country.dto.CountryResponse;
import com.shop.country.service.CountryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** {@link CountryController} implementation. */
@RestController
public class CountryControllerImpl implements CountryController {

    private final CountryService countryService;

    /**
     * Constructs the controller with its required service.
     *
     * @param countryService the country service
     */
    public CountryControllerImpl(CountryService countryService) {
        this.countryService = countryService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<CountryResponse>> listCountries() {
        return ResponseEntity.ok(countryService.listCountries());
    }
}
