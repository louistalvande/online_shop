package com.shop.account.controller.impl;

import com.shop.account.dto.AccountResponse;
import com.shop.account.entity.AccountLanguage;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import com.shop.account.exception.AccountNotFoundException;
import com.shop.account.exception.InvalidAccountStateException;
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

import org.springframework.http.MediaType;

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

    private AccountResponse buildResponse(AccountStatus status) {
        return buildResponse(status, AccountLanguage.FR);
    }

    private AccountResponse buildResponse(AccountStatus status, AccountLanguage language) {
        com.shop.account.entity.Account a = new com.shop.account.entity.Account();
        a.setEmail("bob@example.com");
        a.setFirstName("Bob");
        a.setLastName("Doe");
        a.setRole(AccountRole.BUYER);
        a.setStatus(status);
        a.setLanguage(language);
        return AccountResponse.from(a);
    }

    /** PATCH /{id}/suspend returns 200 with the updated account when the account is ACTIVE. */
    @Test
    void suspendAccount_returns200_whenActive() throws Exception {
        UUID id = UUID.randomUUID();
        given(accountService.suspendAccount(id)).willReturn(buildResponse(AccountStatus.SUSPENDED));

        mvc.perform(patch("/api/admin/accounts/{id}/suspend", id))
                .andExpect(status().isOk());

        then(accountService).should().suspendAccount(id);
    }

    /** PATCH /{id}/suspend returns 409 when the service throws InvalidAccountStateException. */
    @Test
    void suspendAccount_returns409_whenNotActive() throws Exception {
        UUID id = UUID.randomUUID();
        willThrow(new InvalidAccountStateException(id, AccountStatus.ACTIVE))
                .given(accountService).suspendAccount(id);
        given(messageSource.getMessage(anyString(), any(), any(Locale.class))).willReturn("Invalid state");

        mvc.perform(patch("/api/admin/accounts/{id}/suspend", id))
                .andExpect(status().isConflict());
    }

    /** PATCH /{id}/suspend returns 404 when the service throws AccountNotFoundException. */
    @Test
    void suspendAccount_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        willThrow(new AccountNotFoundException(id)).given(accountService).suspendAccount(id);
        given(messageSource.getMessage(anyString(), any(), any(Locale.class))).willReturn("Not found");

        mvc.perform(patch("/api/admin/accounts/{id}/suspend", id))
                .andExpect(status().isNotFound());
    }

    /** PATCH /{id}/reactivate returns 200 with the updated account when the account is SUSPENDED. */
    @Test
    void reactivateAccount_returns200_whenSuspended() throws Exception {
        UUID id = UUID.randomUUID();
        given(accountService.reactivateAccount(id)).willReturn(buildResponse(AccountStatus.ACTIVE));

        mvc.perform(patch("/api/admin/accounts/{id}/reactivate", id))
                .andExpect(status().isOk());

        then(accountService).should().reactivateAccount(id);
    }

    /** PATCH /{id}/reactivate returns 409 when the service throws InvalidAccountStateException. */
    @Test
    void reactivateAccount_returns409_whenNotSuspended() throws Exception {
        UUID id = UUID.randomUUID();
        willThrow(new InvalidAccountStateException(id, AccountStatus.SUSPENDED))
                .given(accountService).reactivateAccount(id);
        given(messageSource.getMessage(anyString(), any(), any(Locale.class))).willReturn("Invalid state");

        mvc.perform(patch("/api/admin/accounts/{id}/reactivate", id))
                .andExpect(status().isConflict());
    }

    /** PATCH /{id}/reactivate returns 404 when the service throws AccountNotFoundException. */
    @Test
    void reactivateAccount_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        willThrow(new AccountNotFoundException(id)).given(accountService).reactivateAccount(id);
        given(messageSource.getMessage(anyString(), any(), any(Locale.class))).willReturn("Not found");

        mvc.perform(patch("/api/admin/accounts/{id}/reactivate", id))
                .andExpect(status().isNotFound());
    }

    /** POST / returns 201 and exposes language=ES in the response body. */
    @Test
    void createAccount_returns201_withLanguageEs() throws Exception {
        given(accountService.createAccount(any())).willReturn(buildResponse(AccountStatus.PENDING, AccountLanguage.ES));

        mvc.perform(post("/api/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"es@example.com\",\"firstName\":\"Carlos\",\"lastName\":\"García\",\"role\":\"BUYER\",\"language\":\"ES\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.language").value("ES"));

        then(accountService).should().createAccount(any());
    }

    /** PATCH /{id} returns 200 and exposes language=ES in the response body. */
    @Test
    void updateAccount_returns200_withLanguageEs() throws Exception {
        UUID id = UUID.randomUUID();
        given(accountService.updateAccount(eq(id), any())).willReturn(buildResponse(AccountStatus.ACTIVE, AccountLanguage.ES));

        mvc.perform(patch("/api/admin/accounts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"language\":\"ES\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("ES"));

        then(accountService).should().updateAccount(eq(id), any());
    }
}
