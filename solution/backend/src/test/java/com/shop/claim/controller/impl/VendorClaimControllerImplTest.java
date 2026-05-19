package com.shop.claim.controller.impl;

import com.shop.claim.dto.ClaimResponse;
import com.shop.claim.entity.ClaimDecision;
import com.shop.claim.entity.ClaimReason;
import com.shop.claim.entity.ClaimStatus;
import com.shop.claim.exception.ClaimNotFoundException;
import com.shop.claim.exception.InvalidClaimStateException;
import com.shop.claim.service.VendorClaimService;
import com.shop.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Unit tests for {@link VendorClaimControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class VendorClaimControllerImplTest {

    @Mock VendorClaimService vendorClaimService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    private static final String VENDOR_EMAIL = "vendor@test.com";
    private static final UUID CLAIM_ID = UUID.randomUUID();

    private final UsernamePasswordAuthenticationToken vendorPrincipal =
            new UsernamePasswordAuthenticationToken(VENDOR_EMAIL, null, List.of());

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new VendorClaimControllerImpl(vendorClaimService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    @Test
    void listClaims_returns200WithList() throws Exception {
        given(vendorClaimService.getVendorClaims(VENDOR_EMAIL))
                .willReturn(List.of(buildResponse(ClaimStatus.OPEN, null)));

        mvc.perform(get("/api/vendor/claims").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    void getClaim_found_returns200() throws Exception {
        given(vendorClaimService.getVendorClaim(VENDOR_EMAIL, CLAIM_ID))
                .willReturn(buildResponse(ClaimStatus.OPEN, null));

        mvc.perform(get("/api/vendor/claims/" + CLAIM_ID).principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CLAIM_ID.toString()));
    }

    @Test
    void getClaim_notFound_returns404() throws Exception {
        given(vendorClaimService.getVendorClaim(VENDOR_EMAIL, CLAIM_ID))
                .willThrow(new ClaimNotFoundException(CLAIM_ID));
        given(messageSource.getMessage(eq("error.claim.not.found"), any(), any(Locale.class)))
                .willReturn("Not found");

        mvc.perform(get("/api/vendor/claims/" + CLAIM_ID).principal(vendorPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CLAIM_NOT_FOUND"));
    }

    @Test
    void grantRefund_returns200WithClosedClaim() throws Exception {
        given(vendorClaimService.grantRefund(eq(VENDOR_EMAIL), eq(CLAIM_ID), any(Locale.class)))
                .willReturn(buildResponse(ClaimStatus.CLOSED, ClaimDecision.GRANTED));

        mvc.perform(post("/api/vendor/claims/" + CLAIM_ID + "/grant").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.decision").value("GRANTED"));
    }

    @Test
    void grantRefund_alreadyClosed_returns409() throws Exception {
        given(vendorClaimService.grantRefund(eq(VENDOR_EMAIL), eq(CLAIM_ID), any(Locale.class)))
                .willThrow(new InvalidClaimStateException(CLAIM_ID, ClaimStatus.CLOSED));
        given(messageSource.getMessage(eq("error.claim.invalid.state"), any(), any(Locale.class)))
                .willReturn("Invalid state");

        mvc.perform(post("/api/vendor/claims/" + CLAIM_ID + "/grant").principal(vendorPrincipal))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_CLAIM_STATE"));
    }

    @Test
    void refuseRefund_returns200WithRefusedClaim() throws Exception {
        given(vendorClaimService.refuseRefund(eq(VENDOR_EMAIL), eq(CLAIM_ID), any(Locale.class)))
                .willReturn(buildResponse(ClaimStatus.CLOSED, ClaimDecision.REFUSED));

        mvc.perform(post("/api/vendor/claims/" + CLAIM_ID + "/refuse").principal(vendorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.decision").value("REFUSED"));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ClaimResponse buildResponse(ClaimStatus status, ClaimDecision decision) {
        ClaimResponse r;
        try {
            var ctor = ClaimResponse.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            r = ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setField(r, "id", CLAIM_ID);
        setField(r, "orderId", UUID.randomUUID());
        setField(r, "orderNumber", "ORD-CLM-VND-TEST");
        setField(r, "buyerId", UUID.randomUUID());
        setField(r, "vendorId", UUID.randomUUID());
        setField(r, "reason", ClaimReason.DEFECTIVE_ITEM);
        setField(r, "message", "Item arrived broken.");
        setField(r, "status", status);
        setField(r, "decision", decision);
        setField(r, "createdAt", LocalDateTime.now());
        setField(r, "updatedAt", LocalDateTime.now());
        return r;
    }

    private static void setField(Object target, String name, Object value) {
        try {
            var f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
