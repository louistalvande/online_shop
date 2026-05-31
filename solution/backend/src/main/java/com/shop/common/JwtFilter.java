package com.shop.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates the JWT on every request and populates the security context.
 *
 * <p>Token resolution order (US-SEC-01 / FS-S03):
 * <ol>
 *   <li>HttpOnly cookie named {@code jwt} — used by browser clients.</li>
 *   <li>{@code Authorization: Bearer} header — accepted for API clients and E2E test runners.</li>
 * </ol>
 *
 * <p>On every valid cookie-based request the token is rotated: a fresh 30-minute cookie is issued
 * and the previous token is immediately blacklisted in Redis (sliding-window session).
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final TokenBlacklistService blacklistService;

    /**
     * @param jwtUtil          JWT generation and validation
     * @param cookieUtil       cookie read/write helpers
     * @param blacklistService Redis-backed token revocation
     */
    public JwtFilter(JwtUtil jwtUtil, CookieUtil cookieUtil,
                     TokenBlacklistService blacklistService) {
        this.jwtUtil          = jwtUtil;
        this.cookieUtil       = cookieUtil;
        this.blacklistService = blacklistService;
    }

    /**
     * Resolves, validates, and rotates the JWT; populates the Spring Security context.
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

        // Bearer takes explicit precedence: API clients that set Authorization header should not
        // trigger cookie rotation (which would blacklist the token they just received from login).
        String bearerToken = null;
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            bearerToken = header.substring(7);
        }

        String cookieToken = cookieUtil.extractFromCookie(request);

        String token;
        boolean fromCookie;
        if (bearerToken != null) {
            token = bearerToken;
            fromCookie = false;
        } else {
            token = cookieToken;
            fromCookie = token != null;
        }

        if (token != null && jwtUtil.isValid(token) && !blacklistService.isBlacklisted(token)) {
            String email = jwtUtil.extractEmail(token);
            String role  = jwtUtil.extractRole(token);

            var auth = new UsernamePasswordAuthenticationToken(
                    email, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Sliding window: rotate the cookie only when less than half the token lifetime remains.
            // Rotating on every request causes concurrent browser requests to race and blacklist each other.
            if (fromCookie) {
                long remainingMs = jwtUtil.extractExpiration(token).getTime() - System.currentTimeMillis();
                long halfLifeMs  = jwtUtil.getExpirationMs() / 2;
                if (remainingMs < halfLifeMs) {
                    blacklistService.blacklist(token, remainingMs);
                    String newToken = jwtUtil.generateToken(email, role);
                    cookieUtil.setJwtCookie(response, newToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
