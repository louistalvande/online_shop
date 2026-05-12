package com.shop.account.controller;

import com.shop.account.dto.AccountResponse;
import com.shop.account.dto.CreateAccountRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin endpoints for managing platform accounts (FS-A01, FS-A02). */
@Tag(name = "Admin — Accounts", description = "Account management for administrators")
@RequestMapping("/api/admin/accounts")
public interface AccountController {

    /**
     * Creates a new buyer or vendor account.
     *
     * @param request the account creation payload
     * @return the created account with HTTP 201
     */
    @Operation(summary = "Create a buyer or vendor account")
    @ApiResponse(responseCode = "201", description = "Account created")
    @ApiResponse(responseCode = "409", description = "Email already used")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping
    ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request);

    /**
     * Returns all platform accounts.
     *
     * @return list of all accounts with HTTP 200
     */
    @Operation(summary = "List all platform accounts")
    @ApiResponse(responseCode = "200", description = "Account list returned")
    @GetMapping
    ResponseEntity<List<AccountResponse>> listAccounts();
}
