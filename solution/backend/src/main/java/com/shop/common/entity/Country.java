package com.shop.common.entity;

import jakarta.persistence.*;

/** Eurozone country reference entry (CS-04). */
@Entity
@Table(name = "countries")
public class Country {

    /** ISO 3166-1 alpha-2 country code (e.g. "FR", "DE"). */
    @Id
    @Column(length = 2)
    private String code;

    /** Country name in French. */
    @Column(name = "name_fr", nullable = false, length = 100)
    private String nameFr;

    /** Country name in English. */
    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    /** @return the ISO 3166-1 alpha-2 country code */
    public String getCode() { return code; }

    /** @return the country name in French */
    public String getNameFr() { return nameFr; }

    /** @return the country name in English */
    public String getNameEn() { return nameEn; }
}
