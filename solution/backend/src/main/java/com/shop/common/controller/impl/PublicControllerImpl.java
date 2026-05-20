package com.shop.common.controller.impl;

import com.shop.carrier.dto.CarrierResponse;
import com.shop.carrier.service.CarrierService;
import com.shop.common.controller.PublicController;
import com.shop.country.service.CountryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Serves public reference-data used by the buyer checkout flow. */
@RestController
public class PublicControllerImpl implements PublicController {

    private final CarrierService carrierService;
    private final CountryService countryService;

    /**
     * Constructs the controller with its service dependencies.
     *
     * @param carrierService service providing carrier data
     * @param countryService service providing Eurozone country data
     */
    public PublicControllerImpl(CarrierService carrierService, CountryService countryService) {
        this.carrierService = carrierService;
        this.countryService = countryService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<CarrierResponse>> listCarriers(String countryCode) {
        return ResponseEntity.ok(carrierService.listActiveForCountry(countryCode));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, String>>> listCountries() {
        List<Map<String, String>> countries = countryService.listCountries().stream()
                .map(c -> Map.of("code", c.getCode(), "nameFr", c.getNameFr(), "nameEn", c.getNameEn()))
                .toList();
        return ResponseEntity.ok(countries);
    }
}
