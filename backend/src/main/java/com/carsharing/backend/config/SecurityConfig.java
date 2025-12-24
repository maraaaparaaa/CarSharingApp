package com.carsharing.backend.config;

import com.carsharing.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * Configurarea completă de securitate pentru aplicație
 *
 * Componente principale:
 * 1. PasswordEncoder - pentru hash-uirea parolelor
 * 2. AuthenticationProvider - verifică credențialele
 * 3. AuthenticationManager - orchestrează autentificarea
 * 4. SecurityFilterChain - definește regulile de acces
 * 5. JwtAuthenticationFilter - interceptează request-urile
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Permite @PreAuthorize pe metode
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Definește regulile de securitate:
     * - Ce endpoint-uri sunt publice?
     * - Ce endpoint-uri necesită autentificare?
     * - Ce endpoint-uri necesită roluri specifice?
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // STEP 1: Dezactivează CSRF (nu avem nevoie pentru REST API cu JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // STEP 2: Permite frame-uri pentru H2 Console
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )

                // STEP 3: Configurează regulile de autorizare
                .authorizeHttpRequests(auth -> auth
                        // ===== ENDPOINT-URI PUBLICE (fără token) =====
                        .requestMatchers(
                                "/api/auth/**",           // /api/auth/register, /api/auth/login
                                "/h2-console/**",         // H2 Console
                                "/api/rides",             // GET toate cursele (vizitatori pot vedea)
                                "/api/rides/{id}",        // GET detalii cursă
                                "/api/rides/search",      // GET căutare curse
                                "/api/rides/upcoming"     // GET curse viitoare
                        ).permitAll()

                        // ===== ENDPOINT-URI PROTEJATE - DOAR ADMIN =====
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/bookings").hasRole("ADMIN")  // GET all bookings

                        // ===== ENDPOINT-URI PROTEJATE - ORICE USER AUTENTIFICAT =====
                        .anyRequest().authenticated()  // Tot restul necesită autentificare
                )

                // STEP 4: Stateless session (nu păstrăm sesiuni server-side)
                // JWT-ul conține toată informația necesară
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // STEP 5: Configurează authentication provider
                .authenticationProvider(authenticationProvider())

                // STEP 6: Adaugă JWT filter ÎNAINTE de UsernamePasswordAuthenticationFilter
                // Acest filtru interceptează FIECARE request și verifică token-ul
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean pentru hash-uirea parolelor cu BCrypt
     *
     * BCrypt caracteristici:
     * - Salt automat (2 useri cu aceeași parolă au hash-uri diferite)
     * - Slow by design (~100ms) - protecție contra brute force
     * - Industry standard
     *
     * Exemplu:
     * Input: "parola123"
     * Output: "$2a$10$N9qo8uLOickgx2ZMRZoMye7Hu3Z6Z3h0KxKj9f0n1Z..."
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationProvider - componenta care VERIFICĂ credențialele
     *
     * Flow la login:
     * 1. User trimite email + password
     * 2. AuthenticationProvider apelează UserDetailsService.loadUserByUsername(email)
     * 3. Primește UserDetails cu password hash-uit
     * 4. Compară: passwordEncoder.matches(password_input, password_hash_din_DB)
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
     * AuthenticationManager - orchestrează procesul de autentificare
     *
     * Folosit în UserService.login() pentru a valida credențialele:
     * authenticationManager.authenticate(
     *     new UsernamePasswordAuthenticationToken(email, password)
     * )
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}