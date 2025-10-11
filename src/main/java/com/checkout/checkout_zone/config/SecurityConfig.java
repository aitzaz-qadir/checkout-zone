package com.checkout.checkout_zone.config;

// Import statements
import com.checkout.checkout_zone.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// Annotations
@Configuration
@EnableWebSecurity

/*
 * Security configuration for the application.
 * Defines access rules for various endpoints based on user roles.
 * Uses HTTP Basic authentication for simplicity.
 * Disables CSRF protection for API endpoints for testing purposes.
 */

// Security configuration class
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disable CSRF for API
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - anyone can access
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/test/**").permitAll()  // Keep test endpoints open for now

                        // Equipment - anyone can view, but only managers/admins can modify
                        .requestMatchers(HttpMethod.GET, "/api/equipment/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/equipment/**").hasAnyRole("EQUIPMENT_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/equipment/**").hasAnyRole("EQUIPMENT_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/equipment/**").hasRole("ADMIN")

                        // Users - only admins can manage users
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // Checkout - users can request, managers can approve/fulfill
                        .requestMatchers(HttpMethod.POST, "/api/checkout/request").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/checkout/requests/user/**").authenticated()
                        .requestMatchers("/api/checkout/requests/*/approve").hasAnyRole("EQUIPMENT_MANAGER", "ADMIN")
                        .requestMatchers("/api/checkout/requests/*/reject").hasAnyRole("EQUIPMENT_MANAGER", "ADMIN")
                        .requestMatchers("/api/checkout/requests/*/fulfill").hasAnyRole("EQUIPMENT_MANAGER", "ADMIN")
                        .requestMatchers("/api/checkout/records/*/return").hasAnyRole("EQUIPMENT_MANAGER", "ADMIN")
                        .requestMatchers("/api/checkout/**").authenticated()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {}) // Use HTTP Basic authentication for testing
                .userDetailsService(userDetailsService); // Set custom user details service

        return http.build();
    }
}