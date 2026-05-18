package com.shop.common.service;

import com.shop.common.entity.Country;

import java.util.List;

/** Provides reference data for Eurozone countries (CS-04). */
public interface CountryService {

    /**
     * Returns all supported Eurozone countries.
     *
     * @return list of all countries in the reference table
     */
    List<Country> listAll();
}
