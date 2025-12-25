package com.carsharing.backend.config;

import com.carsharing.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Complete Security Configuration
 *
 * Components:
 * 1. PasswordEncoder - for password hashing
 * 2. AuthenticationProvider - checks credentials
 * 3. AuthenticationManager - manages authentication
 * 4. SecurityFilterChain - defines access rules
 * 5. JwtAuthenticationFilter - manages the requests
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // allows @PreAuthorize on methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Defines security rules:
     * - What endpoints are public?
     * - What endpoints need authentication?
     * - What endpoints need specified roles?
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disables CSRF (no need for REST API with JWT) - csrf is used only with cookies and sessions
                //                                                  when the site remembers the login ID - with JWT
                //                                                  doesn't automatically send the authentication so we can disable protection
                .csrf(AbstractHttpConfigurer::disable)

                // allows frames for H2 Console
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )

                // Configures authorisation rules
                .authorizeHttpRequests(auth -> auth
                        //  Public endpoints - without token
                        .requestMatchers(
                                "/api/auth/login",
                                "api/auth/register",
                                "/h2-console/**",         // H2 Console
                                "/api/rides",             // GET all rides (visitors can see)
                                "/api/rides/{id}",        // GET ride details
                                "/api/rides/search",      // GET search ride
                                "/api/rides/upcoming"     // GET future ride
                        ).permitAll()
                        // Protected endpoints - only Admin can manage them
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/bookings").hasRole("ADMIN")  // GET all bookings
                        .requestMatchers(HttpMethod.POST, "/api/bookings").authenticated()

                        // - any authenticated user
                        .anyRequest().authenticated()
                )

                // Stateless session
                // JWT contains all necessary info
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configures authentication provider
                .authenticationProvider(authenticationProvider())

                // Adds JWT filter before UsernamePasswordAuthenticationFilter
                // intercepts each request and checks the token
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    /**
     * Bean for password hashing with BCrypt
     *
     * BCrypt :
     * - Auto jump (2 users with the same password have different hashes)
     * - Slow by design (~100ms) - protection against brute force
     * - Industry standard
     *
     * Example:
     * Input: "password123"
     * Output: "$2a$10$N9qo8uLOickgx2ZMRZoMye7Hu3Z6Z3h0KxKj9f0n1Z..."
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationProvider - component that checks credentials
     *
     * Login flow:
     * 1. User sends email + password
     * 2. AuthenticationProvider calls UserDetailsService.loadUserByUsername(email)
     * 3. Gets UserDetails with hashed password
     * 4. Compares: passwordEncoder.matches(password_input, password_hash_din_DB)
     * 5. Match? → SUCCESS | No match? → BadCredentialsException
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager - manages authentication process
     *
     * Used in UserService.login() for credentials validation:
     * authenticationManager.authenticate(
     *     new UsernamePasswordAuthenticationToken(email, password)
     * )
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}