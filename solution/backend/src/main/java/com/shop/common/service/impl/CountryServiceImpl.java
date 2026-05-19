package com.shop.common.service.impl;

import com.shop.common.entity.Country;
import com.shop.common.repository.CountryRepository;
import com.shop.common.service.CountryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** {@link CountryService} implementation backed by the countries reference table. */
@Service
@Transactional(readOnly = true)
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    /**
     * Constructs the service with the country repository.
     *
     * @param countryRepository the JPA repository for country reference data
     */
    public CountryServiceImpl(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    /** {@inheritDoc} */
    @Override
    public List<Country> listAll() {
        return countryRepository.findAll();
    }
}
