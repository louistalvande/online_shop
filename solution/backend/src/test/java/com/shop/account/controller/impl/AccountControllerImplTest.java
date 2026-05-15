package com.shop.account.controller.impl;

import com.shop.account.exception.AccountNotFoundException;
import com.shop.account.service.AccountService;
import com.shop.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Unit tests for {@link AccountControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class AccountControllerImplTest {

    @Mock AccountService accountService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new AccountControllerImpl(accountService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    /** DELETE /{id} returns 204 when the account exists. */
    @Test
    void deleteAccount_returns204_whenFound() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/api/admin/accounts/{id}", id))
                .andExpect(status().isNoContent());

        then(accountService).should().deleteAccount(id);
    }

    /** DELETE /{id} returns 404 when the service throws AccountNotFoundException. */
    @Test
    void deleteAccount_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        willThrow(new AccountNotFoundException(id)).given(accountService).deleteAccount(id);
        given(messageSource.getMessage(anyString(), any(), any(Locale.class))).willReturn("Account not found");

        mvc.perform(delete("/api/admin/accounts/{id}", id))
                .andExpect(status().isNotFound());
    }

    /** GET / returns 200 with the list provided by the service (filtering is the service's concern). */
    @Test
    void listAccounts_returns200() throws Exception {
        given(accountService.listAccounts()).willReturn(List.of());

        mvc.perform(get("/api/admin/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
