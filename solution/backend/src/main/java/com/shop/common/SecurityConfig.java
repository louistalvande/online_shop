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

/**
 * Security configuration — stateless JWT authentication.
 * Admin endpoints require a valid JWT with role ADMIN (CS-08).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain.
     *
     * <ul>
     *   <li>CSRF disabled — stateless API, no session cookies.</li>
     *   <li>{@code POST /api/admin/auth/login} is public.</li>
     *   <li>All other {@code /api/admin/**} routes require role ADMIN.</li>
     *   <li>Swagger UI and actuator health endpoints are public.</li>
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
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/activate").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/setup-password").authenticated()
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
     * BCrypt password encoder used to hash and verify account passwords.
     *
     * @return the BCrypt encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
