package com.shop.carrier.service.impl;

import com.shop.carrier.dto.CarrierResponse;
import com.shop.carrier.dto.CreateCarrierRequest;
import com.shop.carrier.entity.Carrier;
import com.shop.carrier.repository.CarrierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link CarrierServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class CarrierServiceImplTest {

    @Mock CarrierRepository carrierRepository;

    CarrierServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CarrierServiceImpl(carrierRepository);
    }

    private Carrier carrier(String name) {
        Carrier c = new Carrier();
        c.setName(name);
        c.setTrackingUrl("https://example.com/track");
        c.setSupportedCountries(List.of("FR"));
        c.setActive(true);
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }

    /** createCarrier must persist a carrier with active=true and return its DTO. */
    @Test
    void createCarrier_savesActiveCarrierAndReturnsDto() {
        given(carrierRepository.save(any(Carrier.class))).willReturn(carrier("La Poste"));

        CreateCarrierRequest req = new CreateCarrierRequest();
        req.setName("La Poste");
        req.setTrackingUrl("https://example.com/track");
        req.setSupportedCountries(List.of("FR"));

        CarrierResponse result = service.createCarrier(req);

        then(carrierRepository).should().save(any(Carrier.class));
        assertThat(result.getName()).isEqualTo("La Poste");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getSupportedCountries()).containsExactly("FR");
    }

    /** listCarriers must return all carriers as DTOs. */
    @Test
    void listCarriers_returnsMappedDtos() {
        given(carrierRepository.findAll()).willReturn(List.of(carrier("La Poste"), carrier("Colissimo")));

        List<CarrierResponse> result = service.listCarriers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CarrierResponse::getName)
                .containsExactlyInAnyOrder("La Poste", "Colissimo");
    }

    /** listCarriers must return an empty list when no carriers exist. */
    @Test
    void listCarriers_returnsEmptyList_whenNoneExist() {
        given(carrierRepository.findAll()).willReturn(List.of());

        assertThat(service.listCarriers()).isEmpty();
    }
}
