package com.shop.common.repository;

import com.shop.common.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for Eurozone {@link Country} reference data. */
public interface CountryRepository extends JpaRepository<Country, String> {

    /**
     * Checks whether a country code exists in the Eurozone reference table (CS-04).
     *
     * @param code the ISO 3166-1 alpha-2 country code
     * @return {@code true} if the code is a supported Eurozone country
     */
    boolean existsByCode(String code);
}
