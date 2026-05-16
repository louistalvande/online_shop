package com.shop.country.repository;

import com.shop.country.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for {@link Country} reference entities. */
public interface CountryRepository extends JpaRepository<Country, String> {
}
