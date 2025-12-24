package com.carsharing.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts EACH request:
 * 1. Extracts JWT token from header "Authorization"
 * 2. Validates token
 * 3. Sets user in SecurityContext
 * 4. Allows the request to continue to controller
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Extracts the Authorization header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Checks if the header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token -> continue without authentication
            filterChain.doFilter(request, response);
            return;
        }

        //Extracts the token (after "Bearer ")
        jwt = authHeader.substring(7); // "Bearer eyJhbG..." → "eyJhbG..."

        //Extracts email from token
        userEmail = jwtService.extractEmail(jwt);

        //Checks if user is already authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            //Tries the user from DB
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            //Validates token
            if (jwtService.isTokenValid(jwt, userDetails)) {

                //Creates authentication object
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,           // Principal (user)
                        null,                  // Credentials (we do not need a password anymore)
                        userDetails.getAuthorities() // Authorities (roles)
                );

                // Adds details about request (IP, session, etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Sets authentication in SecurityContext
                // Controller knows who the user is
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue with the next filter/controller
        filterChain.doFilter(request, response);
    }
}