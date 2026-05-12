package com.shop.auth.controller;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** Authentication endpoints for the admin console. */
@Tag(name = "Admin Auth", description = "Admin console authentication")
@RequestMapping("/api/admin/auth")
public interface AuthController {

    /**
     * Authenticates an admin account and returns a JWT.
     *
     * @param request login credentials
     * @return 200 with {@link AuthResponse} on success, 401 if credentials are invalid
     */
    @Operation(summary = "Admin login — returns a signed JWT on success")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive account")
    })
    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);
}
