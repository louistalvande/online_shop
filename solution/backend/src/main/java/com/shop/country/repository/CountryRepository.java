package com.shop.country.repository;

import com.shop.country.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for {@link Country} reference entities. */
public interface CountryRepository extends JpaRepository<Country, String> {

    /**
     * Checks whether a country code exists in the Eurozone reference table (CS-04).
     *
     * @param code the ISO 3166-1 alpha-2 country code
     * @return {@code true} if the code is a supported Eurozone country
     */
    boolean existsByCode(String code);
}
