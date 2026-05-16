package com.shop.country.service.impl;

import com.shop.country.dto.CountryResponse;
import com.shop.country.repository.CountryRepository;
import com.shop.country.service.CountryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** {@link CountryService} implementation. */
@Service
@Transactional(readOnly = true)
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    /**
     * Constructs the service with its required repository.
     *
     * @param countryRepository the country JPA repository
     */
    public CountryServiceImpl(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    /** {@inheritDoc} */
    @Override
    public List<CountryResponse> listCountries() {
        return countryRepository.findAll().stream()
                .map(CountryResponse::from)
                .toList();
    }
}
