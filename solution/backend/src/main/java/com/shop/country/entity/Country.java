package com.shop.country.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Eurozone country reference — ISO 3166-1 alpha-2 (CS-04). */
@Entity
@Table(name = "countries")
public class Country {

    /** ISO 3166-1 alpha-2 country code (primary key). */
    @Id
    @Column(length = 2)
    private String code;

    /** Country name in French. */
    @Column(name = "name_fr", nullable = false, length = 100)
    private String nameFr;

    /** Country name in English. */
    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    /**
     * Returns the ISO 3166-1 alpha-2 country code.
     *
     * @return the country code
     */
    public String getCode() { return code; }

    /**
     * Returns the French country name.
     *
     * @return French name
     */
    public String getNameFr() { return nameFr; }

    /**
     * Returns the English country name.
     *
     * @return English name
     */
    public String getNameEn() { return nameEn; }
}
