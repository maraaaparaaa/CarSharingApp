package com.carsharing.backend.service;

import com.carsharing.backend.dto.AuthResponse;
import com.carsharing.backend.dto.LoginRequest;
import com.carsharing.backend.dto.RegisterRequest;
import com.carsharing.backend.exception.InvalidBookingException;
import com.carsharing.backend.exception.ResourceNotFoundException;
import com.carsharing.backend.model.Ride;
import com.carsharing.backend.model.User;
import com.carsharing.backend.repository.BookingRepository;
import com.carsharing.backend.repository.RideRepository;
import com.carsharing.backend.repository.UserRepository;
import com.carsharing.backend.security.CustomUserDetails;
import com.carsharing.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.carsharing.backend.config.SecurityConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for user management
 * - Register
 * - Login
 * - User management
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user
     *
     * Validation
     * Hash password
     * Save user
     * Generates JWT token
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidBookingException("Email already registered");
        }

        if (!isValidEmail(request.getEmail())) {
            throw new InvalidBookingException("Invalid email format");
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new InvalidBookingException("Password must be at least 6 characters long");
        }

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new InvalidBookingException("Full name is required");
        }

        // creates new user
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // HASH password!
        user.setFullName(request.getFullName().trim());
        user.setPhoneNumber(request.getPhoneNumber());

        // role default : User
        user.setRole(request.getRole() != null ? request.getRole() : User.UserRole.USER);

        // saves in DB
        User savedUser = userRepository.save(user);

        // Generates JWT token with custom claims
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", savedUser.getId());
        extraClaims.put("role", savedUser.getRole().name());

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String jwtToken = jwtService.generateToken(extraClaims, userDetails);

        // Returns token + info user
        return new AuthResponse(jwtToken, savedUser);
    }

    /**
     * Authenticates existing user
     *
     * Checks credentials (email + password)
     * Generates new JWT token
     */
    public AuthResponse login(LoginRequest request) {
        // Spring Security automatically cheks credentials
        // throws BadCredentialsException if wrong credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Correct credentials
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Generates JWT token
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", user.getId());
        extraClaims.put("role", user.getRole().name());

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(extraClaims, userDetails);

        return new AuthResponse(jwtToken, user);
    }

    /**
     * Gets all users (only for ADMIN)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));
    }

    /**
     * Deletes user (only for ADMIN)
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User with id " + id + " not found");
        }

        // deletes bookings where the user is passenger
        bookingRepository.deleteAllByPassengerId(id);

        // obtains all users' rides
        List<Ride> rides = rideRepository.findByDriverId(id);

        // deletes all bookings for those rides
        for (Ride ride : rides) {
            bookingRepository.deleteAllByRideId(ride.getId());
        }

        // deletes the rides
        rideRepository.deleteAllByDriverId(id);

        // deletes the user
        userRepository.deleteById(id);
    }

    /**
     * Format email validation
     */
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}