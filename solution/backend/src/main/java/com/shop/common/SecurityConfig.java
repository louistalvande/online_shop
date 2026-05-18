package com.shop.common;

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

/**
 * Security configuration — stateless JWT authentication with ANSSI-compliant hardening.
 * BCrypt cost 12 (CPA-10), security headers (CPA-09), role-based access control (CS-08).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain.
     *
     * <ul>
     *   <li>CSRF disabled — stateless JWT API; CSRF covered by {@code SameSite=Strict} at Nginx level (CPA-12).</li>
     *   <li>Security response headers applied on every response (CPA-09 / SEC-TLS-001..003).</li>
     *   <li>Session stateless — no server-side session state.</li>
     *   <li>Role-based access: ADMIN, VENDOR, or public.</li>
     * </ul>
     *
     * @param http      the HttpSecurity to configure
     * @param jwtFilter the filter that validates Bearer tokens
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
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
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data:; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'; " +
                        "form-action 'self'"))
                // Referrer-Policy — no leakage across origins
                .referrerPolicy(ref -> ref
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/activate").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/setup-password").authenticated()
                .requestMatchers("/api/me", "/api/me/**").authenticated()
                .requestMatchers("/api/cart", "/api/cart/**").hasRole("BUYER")
                .requestMatchers("/api/orders", "/api/orders/**").hasRole("BUYER")
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/actuator/health", "/actuator/info").permitAll()
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
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * BCrypt password encoder with cost factor 12, compliant with CPA-10 / SEC-AUTH-002.
     * Upgraded from cost 10 to strengthen resistance against brute-force cracking.
     *
     * @return the BCrypt encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
