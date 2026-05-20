package com.shop.claim.controller.impl;

import com.shop.claim.dto.ClaimResponse;
import com.shop.claim.entity.ClaimDecision;
import com.shop.claim.entity.ClaimReason;
import com.shop.claim.entity.ClaimStatus;
import com.shop.claim.exception.ClaimAlreadyOpenException;
import com.shop.claim.service.ClaimService;
import com.shop.common.GlobalExceptionHandler;
import com.shop.order.exception.InvalidOrderStateException;
import com.shop.order.entity.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
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

/** Unit tests for {@link ClaimControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class ClaimControllerImplTest {

    @Mock ClaimService claimService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    private static final String BUYER_EMAIL = "buyer@test.com";
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CLAIM_ID = UUID.randomUUID();

    private final UsernamePasswordAuthenticationToken buyerPrincipal =
            new UsernamePasswordAuthenticationToken(BUYER_EMAIL, null, List.of());

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new ClaimControllerImpl(claimService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    @Test
    void openClaim_returns201WithCreatedClaim() throws Exception {
        given(claimService.openClaim(eq(BUYER_EMAIL), eq(ORDER_ID), any(), any(Locale.class)))
                .willReturn(buildResponse(ClaimStatus.OPEN, null));

        mvc.perform(post("/api/orders/" + ORDER_ID + "/claims")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"NON_RECEIPT\",\"message\":\"My order never arrived.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void openClaim_invalidState_returns409() throws Exception {
        given(claimService.openClaim(eq(BUYER_EMAIL), eq(ORDER_ID), any(), any(Locale.class)))
                .willThrow(new InvalidOrderStateException(ORDER_ID, OrderStatus.CANCELLED));
        given(messageSource.getMessage(eq("error.order.invalid.state"), any(), any(Locale.class)))
                .willReturn("Invalid state");

        mvc.perform(post("/api/orders/" + ORDER_ID + "/claims")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"NON_RECEIPT\",\"message\":\"My order never arrived.\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_ORDER_STATE"));
    }

    @Test
    void openClaim_alreadyOpen_returns409() throws Exception {
        given(claimService.openClaim(eq(BUYER_EMAIL), eq(ORDER_ID), any(), any(Locale.class)))
                .willThrow(new ClaimAlreadyOpenException(ORDER_ID));
        given(messageSource.getMessage(eq("error.claim.already.open"), any(), any(Locale.class)))
                .willReturn("Claim already open");

        mvc.perform(post("/api/orders/" + ORDER_ID + "/claims")
                        .principal(buyerPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"NON_RECEIPT\",\"message\":\"My order never arrived.\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CLAIM_ALREADY_OPEN"));
    }

    @Test
    void getOrderClaims_returns200WithList() throws Exception {
        given(claimService.getMyClaims(BUYER_EMAIL))
                .willReturn(List.of(buildResponse(ClaimStatus.OPEN, null)));

        mvc.perform(get("/api/orders/" + ORDER_ID + "/claims").principal(buyerPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("OPEN"));
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
        setField(r, "orderId", ORDER_ID);
        setField(r, "orderNumber", "ORD-CLM-TEST");
        setField(r, "buyerId", UUID.randomUUID());
        setField(r, "vendorId", UUID.randomUUID());
        setField(r, "reason", ClaimReason.NON_RECEIPT);
        setField(r, "message", "My order never arrived.");
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
