package com.shop.country.dto;

import com.shop.country.entity.Country;
import io.swagger.v3.oas.annotations.media.Schema;

/** Country data returned by the API. */
public class CountryResponse {

    @Schema(description = "ISO 3166-1 alpha-2 country code")
    private String code;

    @Schema(description = "Country name in French")
    private String nameFr;

    @Schema(description = "Country name in English")
    private String nameEn;

    /**
     * Builds a {@link CountryResponse} from a {@link Country} entity.
     *
     * @param c the country entity
     * @return the corresponding DTO
     */
    public static CountryResponse from(Country c) {
        CountryResponse r = new CountryResponse();
        r.code = c.getCode();
        r.nameFr = c.getNameFr();
        r.nameEn = c.getNameEn();
        return r;
    }

    /** @return ISO 3166-1 alpha-2 code */
    public String getCode() { return code; }

    /** @return French country name */
    public String getNameFr() { return nameFr; }

    /** @return English country name */
    public String getNameEn() { return nameEn; }
}
