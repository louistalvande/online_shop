package com.shop.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.settings.service.SettingsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Intercepts all requests when maintenance mode is active (US-ADM-10 / FS-A05).
 * Returns HTTP 503 to any non-admin caller that attempts to reach a protected endpoint.
 * Admin users and requests to public or auth paths pass through unaffected.
 */
@Component
public class MaintenanceFilter extends OncePerRequestFilter {

    private final SettingsService settingsService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the filter with the settings service and Jackson mapper.
     *
     * @param settingsService checks the current maintenance mode state
     * @param objectMapper    serialises the 503 error body
     */
    public MaintenanceFilter(SettingsService settingsService, ObjectMapper objectMapper) {
        this.settingsService = settingsService;
        this.objectMapper = objectMapper;
    }

    /**
     * Passes through the request when maintenance mode is inactive, when the request targets
     * a public/auth/actuator path, or when the caller holds the ADMIN role.
     * Otherwise writes a 503 JSON response and stops the chain.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the remaining filter chain
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!settingsService.isMaintenanceActive()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // Always allow public/auth/infra paths regardless of maintenance state
        if (isPassThroughPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Allow ADMIN users full access during maintenance
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                Map.of("error", "SERVICE_UNAVAILABLE",
                        "message", "The platform is currently under maintenance. Please try again later."));
    }

    /** Returns true for paths that must bypass maintenance mode. */
    private boolean isPassThroughPath(String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/public/")
                || path.startsWith("/api/admin/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/api-docs/")
                || path.startsWith("/actuator/");
    }
}
