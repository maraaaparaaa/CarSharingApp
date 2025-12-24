package com.carsharing.backend.security;

import com.carsharing.backend.model.User;
import com.carsharing.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service that loads the user in DB for Spring Security
 * Spring Security calls loadUserByUsername() when:
 * 1. User logs in (checks credentials)
 * 2. JwtAuthFilter validates the token (tries the user)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user by email (Spring Security names email "username")
     *
     * Flow:
     * 1. Spring Security asks: "Get user with username = ion@test.com"
     * 2. Search in DB: userRepository.findByEmail("ion@test.com")
     * 3. If found -> wrap User in CustomUserDetails
     * 4. If not -> throw UsernameNotFoundException
     *
     * @param username - email in our case
     * @return UserDetails - wrapper over User for Spring Security
     * @throws UsernameNotFoundException if user does not exist
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return new CustomUserDetails(user);
    }
    // Spring Security compares password from request with password from hash
}