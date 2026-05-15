package com.shop.account.controller;

import com.shop.account.dto.AccountResponse;
import com.shop.account.dto.CreateAccountRequest;
import com.shop.account.dto.UpdateAccountRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    /**
     * Updates the editable fields of an account (FS-A01 / FS-A02 / CS-10).
     * Only non-null fields in the request are applied.
     *
     * @param id      the account UUID
     * @param request the fields to update
     * @return the updated account with HTTP 200
     */
    @Operation(summary = "Update an account's editable fields (name, role, language)")
    @ApiResponse(responseCode = "200", description = "Account updated")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PatchMapping("/{id}")
    ResponseEntity<AccountResponse> updateAccount(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateAccountRequest request);

    /**
     * Soft-deletes an account by setting its status to {@code DELETED}.
     *
     * @param id the account UUID
     * @return HTTP 204 No Content
     */
    @Operation(summary = "Soft-delete an account (sets status to DELETED)")
    @ApiResponse(responseCode = "204", description = "Account deleted")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteAccount(@PathVariable UUID id);
}
