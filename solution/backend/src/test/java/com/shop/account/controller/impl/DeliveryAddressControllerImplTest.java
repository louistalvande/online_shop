package com.shop.account.controller.impl;

import com.shop.account.dto.DeliveryAddressRequest;
import com.shop.account.dto.DeliveryAddressResponse;
import com.shop.account.service.DeliveryAddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

/** Unit tests for {@link DeliveryAddressControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class DeliveryAddressControllerImplTest {

    @Mock DeliveryAddressService service;

    DeliveryAddressControllerImpl controller;

    private static final String BUYER_EMAIL = "buyer@test.com";
    private static final UUID ADDRESS_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new DeliveryAddressControllerImpl(service);
    }

    private Principal principal() {
        return () -> BUYER_EMAIL;
    }

    // ─── listAddresses ────────────────────────────────────────────────────────

    @Test
    void listAddresses_returns200WithList() {
        DeliveryAddressResponse dto = buildResponse();
        given(service.listAddresses(BUYER_EMAIL)).willReturn(List.of(dto));

        ResponseEntity<List<DeliveryAddressResponse>> response = controller.listAddresses(principal());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(dto);
    }

    // ─── createAddress ────────────────────────────────────────────────────────

    @Test
    void createAddress_returns201WithBody() {
        DeliveryAddressRequest req = buildRequest();
        DeliveryAddressResponse dto = buildResponse();
        given(service.createAddress(BUYER_EMAIL, req)).willReturn(dto);

        ResponseEntity<DeliveryAddressResponse> response = controller.createAddress(principal(), req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    // ─── updateAddress ────────────────────────────────────────────────────────

    @Test
    void updateAddress_returns200WithBody() {
        DeliveryAddressRequest req = buildRequest();
        DeliveryAddressResponse dto = buildResponse();
        given(service.updateAddress(BUYER_EMAIL, ADDRESS_ID, req)).willReturn(dto);

        ResponseEntity<DeliveryAddressResponse> response =
                controller.updateAddress(principal(), ADDRESS_ID, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    // ─── deleteAddress ────────────────────────────────────────────────────────

    @Test
    void deleteAddress_returns204NoContent() {
        willDoNothing().given(service).deleteAddress(BUYER_EMAIL, ADDRESS_ID);

        ResponseEntity<Void> response = controller.deleteAddress(principal(), ADDRESS_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(service).should().deleteAddress(BUYER_EMAIL, ADDRESS_ID);
    }

    // ─── setDefault ───────────────────────────────────────────────────────────

    @Test
    void setDefault_returns200WithBody() {
        DeliveryAddressResponse dto = buildResponse();
        given(service.setDefault(BUYER_EMAIL, ADDRESS_ID)).willReturn(dto);

        ResponseEntity<DeliveryAddressResponse> response =
                controller.setDefault(principal(), ADDRESS_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private DeliveryAddressRequest buildRequest() {
        DeliveryAddressRequest r = new DeliveryAddressRequest();
        r.setLabel("Home");
        r.setAddressLine("1 rue Test");
        r.setCity("Paris");
        r.setPostalCode("75001");
        r.setCountryCode("FR");
        r.setMakeDefault(false);
        return r;
    }

    private DeliveryAddressResponse buildResponse() {
        return DeliveryAddressResponse.from(buildAddress());
    }

    private com.shop.account.entity.DeliveryAddress buildAddress() {
        com.shop.account.entity.Account owner = new com.shop.account.entity.Account();
        try {
            var f = owner.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(owner, UUID.randomUUID());
        } catch (Exception e) { throw new RuntimeException(e); }

        com.shop.account.entity.DeliveryAddress a = new com.shop.account.entity.DeliveryAddress();
        try {
            var f = a.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(a, ADDRESS_ID);
        } catch (Exception e) { throw new RuntimeException(e); }
        a.setAccount(owner);
        a.setLabel("Home");
        a.setAddressLine("1 rue Test");
        a.setCity("Paris");
        a.setPostalCode("75001");
        a.setCountryCode("FR");
        a.setDefault(false);
        return a;
    }
}
