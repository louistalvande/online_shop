package com.shop.common;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration — stateless JWT authentication with ANSSI-compliant hardening.
 * BCrypt cost 12 (CPA-10), security headers (CPA-09), CORS restriction (US-SEC-02 / FS-S05 / CPA-12),
 * role-based access control (CS-08).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final List<String> allowedOrigins;

    /**
     * @param allowedOrigins comma-separated list of frontend origins permitted to make credentialed
     *                       cross-origin requests (US-SEC-02 / CPA-12)
     */
    public SecurityConfig(
            @Value("${app.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:5175,http://buyer.localhost,http://vendor.localhost,http://admin.localhost}") String allowedOrigins) {
        this.allowedOrigins = List.of(allowedOrigins.split(","));
    }

    /**
     * Configures the security filter chain.
     *
     * <ul>
     *   <li>CORS — only the configured frontend origins are allowed; OPTIONS preflight returns 403 for unknown origins (US-SEC-02 / CPA-12).</li>
     *   <li>CSRF disabled — mitigated by {@code SameSite=Strict} cookies (US-SEC-01 / CPA-12).</li>
     *   <li>Security response headers applied on every response (CPA-09 / SEC-TLS-001..003).</li>
     *   <li>Session stateless — no server-side session state.</li>
     *   <li>Role-based access: ADMIN, VENDOR, or public.</li>
     * </ul>
     *
     * @param http               the HttpSecurity to configure
     * @param jwtFilter          the filter that validates JWT tokens
     * @param maintenanceFilter  the filter that blocks non-admin traffic during maintenance
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter,
                                           MaintenanceFilter maintenanceFilter) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                // HSTS — 1 year, include subdomains (CPA-09 / SEC-TLS-002)
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31_536_000))
                // Deny embedding in iframes (CPA-09 / SEC-APP-004)
                .frameOptions(frame -> frame.deny())
                // Prevent MIME-type sniffing (CPA-09)
                .contentTypeOptions(ct -> {})
                // Content-Security-Policy — restrict resource origins (CS-14 / SEC-APP-005)
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self'; " +
                        "style-src 'self'; " +
                        "img-src 'self' data:; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'; " +
                        "form-action 'self'"))
                // Referrer-Policy — no leakage across origins
                .referrerPolicy(ref -> ref
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/activate").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/resend-activation").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/mfa/verify").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/setup-password").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/auth/mfa/setup/init").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/auth/mfa/setup/confirm").authenticated()
                .requestMatchers("/api/me", "/api/me/**").authenticated()
                .requestMatchers("/api/cart", "/api/cart/**").hasRole("BUYER")
                .requestMatchers("/api/orders", "/api/orders/**").hasRole("BUYER")
                .requestMatchers("/api/profile/**").hasRole("BUYER")
                .requestMatchers(HttpMethod.GET, "/api/buyer/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/carriers").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/countries").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/announcements").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs", "/api-docs/**", "/api-docs.yaml", "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET,    "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,   "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,    "/api/vendor/**").hasRole("VENDOR")
                .requestMatchers(HttpMethod.POST,   "/api/vendor/**").hasRole("VENDOR")
                .requestMatchers(HttpMethod.PUT,    "/api/vendor/**").hasRole("VENDOR")
                .requestMatchers(HttpMethod.PATCH,  "/api/vendor/**").hasRole("VENDOR")
                .requestMatchers(HttpMethod.DELETE, "/api/vendor/**").hasRole("VENDOR")
                .anyRequest().authenticated()
            )
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint((req, res, ex) -> res.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                .accessDeniedHandler((req, res, ex) -> res.setStatus(HttpServletResponse.SC_FORBIDDEN)))
            // JWT runs first so the security context is populated before MaintenanceFilter checks the role
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(maintenanceFilter, JwtFilter.class);
        return http.build();
    }

    /**
     * CORS policy: credentials allowed only from the configured frontend origins (US-SEC-02 / FS-S05 / CPA-12).
     * Preflight (OPTIONS) requests from any other origin receive a {@code 403 Forbidden} response.
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    /**
     * BCrypt password encoder with cost factor 12, compliant with CPA-10 / SEC-AUTH-002.
     *
     * @return the BCrypt encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
