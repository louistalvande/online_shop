package com.shop.carrier.controller.impl;

import com.shop.carrier.dto.CarrierResponse;
import com.shop.carrier.entity.Carrier;
import com.shop.carrier.exception.CarrierNotFoundException;
import com.shop.carrier.service.CarrierService;
import com.shop.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Unit tests for {@link CarrierControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class CarrierControllerImplTest {

    @Mock CarrierService carrierService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new CarrierControllerImpl(carrierService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    private CarrierResponse buildResponse(String name) {
        Carrier c = new Carrier();
        c.setName(name);
        c.setTrackingUrl("https://example.com/track");
        c.setSupportedCountries(List.of("FR"));
        c.setActive(true);
        c.setCreatedAt(LocalDateTime.now());
        return CarrierResponse.from(c);
    }

    /** POST / returns 201 with valid payload. */
    @Test
    void createCarrier_returns201_whenValid() throws Exception {
        given(carrierService.createCarrier(any())).willReturn(buildResponse("La Poste"));

        mvc.perform(post("/api/admin/carriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"La Poste","trackingUrl":"https://example.com","supportedCountries":["FR"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("La Poste"))
                .andExpect(jsonPath("$.active").value(true));
    }

    /** POST / returns 400 when name is blank. */
    @Test
    void createCarrier_returns400_whenNameBlank() throws Exception {
        mvc.perform(post("/api/admin/carriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","trackingUrl":"https://example.com","supportedCountries":["FR"]}
                                """))
                .andExpect(status().isBadRequest());
    }

    /** POST / returns 400 when supportedCountries is empty. */
    @Test
    void createCarrier_returns400_whenNoCountries() throws Exception {
        mvc.perform(post("/api/admin/carriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"DHL","trackingUrl":"https://example.com","supportedCountries":[]}
                                """))
                .andExpect(status().isBadRequest());
    }

    /** GET / returns 200 with the carrier list. */
    @Test
    void listCarriers_returns200() throws Exception {
        given(carrierService.listCarriers()).willReturn(List.of(buildResponse("La Poste")));

        mvc.perform(get("/api/admin/carriers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("La Poste"));
    }

    /** PATCH /{id} returns 200 with updated carrier. */
    @Test
    void updateCarrier_returns200_whenValid() throws Exception {
        UUID id = UUID.randomUUID();
        CarrierResponse updated = buildResponse("DHL");
        given(carrierService.updateCarrier(eq(id), any())).willReturn(updated);

        mvc.perform(patch("/api/admin/carriers/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"DHL","trackingUrl":"https://dhl.com","supportedCountries":["FR","DE"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("DHL"));
    }

    /** PATCH /{id} returns 404 when carrier does not exist. */
    @Test
    void updateCarrier_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        given(carrierService.updateCarrier(eq(id), any())).willThrow(new CarrierNotFoundException(id));
        given(messageSource.getMessage(eq("error.carrier.not.found"), isNull(), any(Locale.class)))
                .willReturn("Carrier not found.");

        mvc.perform(patch("/api/admin/carriers/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"X","trackingUrl":"https://x.com","supportedCountries":["FR"]}
                                """))
                .andExpect(status().isNotFound());
    }

    /** PATCH /{id}/deactivate returns 200 with active=false. */
    @Test
    void deactivateCarrier_returns200_whenExists() throws Exception {
        UUID id = UUID.randomUUID();
        Carrier c = new Carrier();
        c.setName("La Poste");
        c.setTrackingUrl("https://example.com/track");
        c.setSupportedCountries(List.of("FR"));
        c.setActive(false);
        c.setCreatedAt(LocalDateTime.now());
        given(carrierService.deactivateCarrier(id)).willReturn(CarrierResponse.from(c));

        mvc.perform(patch("/api/admin/carriers/" + id + "/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    /** PATCH /{id}/activate returns 200 with active=true. */
    @Test
    void activateCarrier_returns200_whenExists() throws Exception {
        UUID id = UUID.randomUUID();
        given(carrierService.activateCarrier(id)).willReturn(buildResponse("La Poste"));

        mvc.perform(patch("/api/admin/carriers/" + id + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    /** DELETE /{id} returns 204. */
    @Test
    void deleteCarrier_returns204_whenExists() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/api/admin/carriers/" + id))
                .andExpect(status().isNoContent());
    }

    /** DELETE /{id} returns 404 when carrier does not exist. */
    @Test
    void deleteCarrier_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        willThrow(new CarrierNotFoundException(id)).given(carrierService).deleteCarrier(id);
        given(messageSource.getMessage(eq("error.carrier.not.found"), isNull(), any(Locale.class)))
                .willReturn("Carrier not found.");

        mvc.perform(delete("/api/admin/carriers/" + id))
                .andExpect(status().isNotFound());
    }
}
