package com.shop.carrier.service.impl;

import com.shop.carrier.dto.CarrierResponse;
import com.shop.carrier.dto.CreateCarrierRequest;
import com.shop.carrier.dto.UpdateCarrierRequest;
import com.shop.carrier.entity.Carrier;
import com.shop.carrier.exception.CarrierNotFoundException;
import com.shop.carrier.repository.CarrierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link CarrierServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class CarrierServiceImplTest {

    @Mock CarrierRepository carrierRepository;

    CarrierServiceImpl service;

    private static final UUID ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new CarrierServiceImpl(carrierRepository);
    }

    private Carrier carrier(String name) {
        Carrier c = new Carrier();
        c.setName(name);
        c.setTrackingUrl("https://example.com/track");
        c.setSupportedCountries(new java.util.ArrayList<>(List.of("FR")));
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

    /** updateCarrier must update all fields and return the updated DTO. */
    @Test
    void updateCarrier_updatesFieldsAndReturnsDto() {
        Carrier existing = carrier("La Poste");
        given(carrierRepository.findById(ID)).willReturn(Optional.of(existing));
        given(carrierRepository.save(any(Carrier.class))).willAnswer(inv -> inv.getArgument(0));

        UpdateCarrierRequest req = new UpdateCarrierRequest();
        req.setName("DHL");
        req.setTrackingUrl("https://dhl.com/track");
        req.setSupportedCountries(List.of("FR", "DE"));

        CarrierResponse result = service.updateCarrier(ID, req);

        assertThat(result.getName()).isEqualTo("DHL");
        assertThat(result.getTrackingUrl()).isEqualTo("https://dhl.com/track");
        assertThat(result.getSupportedCountries()).containsExactlyInAnyOrder("FR", "DE");
    }

    /** updateCarrier must throw CarrierNotFoundException when the carrier does not exist. */
    @Test
    void updateCarrier_throwsCarrierNotFoundException_whenNotFound() {
        given(carrierRepository.findById(ID)).willReturn(Optional.empty());

        UpdateCarrierRequest req = new UpdateCarrierRequest();
        req.setName("X");
        req.setTrackingUrl("https://x.com");
        req.setSupportedCountries(List.of("FR"));

        assertThatThrownBy(() -> service.updateCarrier(ID, req))
                .isInstanceOf(CarrierNotFoundException.class);
    }

    /** deactivateCarrier must set active to false and return the updated DTO. */
    @Test
    void deactivateCarrier_setsActiveFalseAndReturnsDto() {
        Carrier existing = carrier("La Poste");
        given(carrierRepository.findById(ID)).willReturn(Optional.of(existing));
        given(carrierRepository.save(any(Carrier.class))).willAnswer(inv -> inv.getArgument(0));

        CarrierResponse result = service.deactivateCarrier(ID);

        assertThat(result.isActive()).isFalse();
    }

    /** deactivateCarrier must throw CarrierNotFoundException when the carrier does not exist. */
    @Test
    void deactivateCarrier_throwsCarrierNotFoundException_whenNotFound() {
        given(carrierRepository.findById(ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivateCarrier(ID))
                .isInstanceOf(CarrierNotFoundException.class);
    }

    /** activateCarrier must set active to true and return the updated DTO. */
    @Test
    void activateCarrier_setsActiveTrueAndReturnsDto() {
        Carrier existing = carrier("La Poste");
        existing.setActive(false);
        given(carrierRepository.findById(ID)).willReturn(Optional.of(existing));
        given(carrierRepository.save(any(Carrier.class))).willAnswer(inv -> inv.getArgument(0));

        CarrierResponse result = service.activateCarrier(ID);

        assertThat(result.isActive()).isTrue();
    }

    /** activateCarrier must throw CarrierNotFoundException when the carrier does not exist. */
    @Test
    void activateCarrier_throwsCarrierNotFoundException_whenNotFound() {
        given(carrierRepository.findById(ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.activateCarrier(ID))
                .isInstanceOf(CarrierNotFoundException.class);
    }

    /** deleteCarrier must call deleteById when the carrier exists. */
    @Test
    void deleteCarrier_deletesCarrierWhenExists() {
        given(carrierRepository.existsById(ID)).willReturn(true);

        service.deleteCarrier(ID);

        then(carrierRepository).should().deleteById(ID);
    }

    /** deleteCarrier must throw CarrierNotFoundException when the carrier does not exist. */
    @Test
    void deleteCarrier_throwsCarrierNotFoundException_whenNotFound() {
        given(carrierRepository.existsById(ID)).willReturn(false);

        assertThatThrownBy(() -> service.deleteCarrier(ID))
                .isInstanceOf(CarrierNotFoundException.class);
    }
}
