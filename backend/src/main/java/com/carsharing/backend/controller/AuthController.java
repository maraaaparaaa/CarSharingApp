package com.carsharing.backend.controller;

import com.carsharing.backend.dto.AuthResponse;
import com.carsharing.backend.dto.LoginRequest;
import com.carsharing.backend.dto.RegisterRequest;
import com.carsharing.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication:
 * - POST /api/auth/register - register new user
 * - POST /api/auth/login - login existing user
 *
 * Both endpoints are public (do not need JWT token)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    /**
     * POST /api/auth/register
     *
     * Request body:
     * {
     *   "email": "ion@test.com",
     *   "password": "parola123",
     *   "fullName": "Ion Popescu",
     *   "phoneNumber": "0712345678"  // optional
     * }
     *
     * Response (201 CREATED):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "id": 1,
     *   "email": "ion@test.com",
     *   "fullName": "Ion Popescu",
     *   "role": "USER"
     * }
     *
     * Possible Errors:
     * - 400 BAD REQUEST: Email already exists / invalid format / short passwor
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     *
     * Request body:
     * {
     *   "email": "ion@test.com",
     *   "password": "parola123"
     * }
     *
     * Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "id": 1,
     *   "email": "ion@test.com",
     *   "fullName": "Ion Popescu",
     *   "role": "USER"
     * }
     *
     * Possible errors:
     * - 401 UNAUTHORIZED: wrong email or password
     * - 404 NOT FOUND: user does not exist
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/test
     * Test endpoint for checking if authentication works
     * Protected - needs JWT token
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Authentication works! You are logged in.");
    }
}