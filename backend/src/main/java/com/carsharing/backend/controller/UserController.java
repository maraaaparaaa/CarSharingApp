package com.carsharing.backend.controller;

import com.carsharing.backend.model.User;
import com.carsharing.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for users management
 *
 * All endpoints are protected:
 * - Needs valid JWT token
 * - Some need specific role (ADMIN)
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users
     * Gets all users (only ADMIN)
     *
     * Required headers:
     * Authorization: Bearer eyJhbGci...
     *
     * Required role: ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")  // only ADMIN can have access - checked before executing the method
                                    // if the user is not admin -> 403 FORBIDDEN
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/5
     * Gets user by ID
     *
     * Any authenticated user can access
     * (Required in reality:
     *  - User needs to see only one's profile
     *  - ADMIN can see any profile)
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/users/email/ion@test.com
     * Get user by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * DELETE /api/users/5
     * Delete user (only ADMIN)
     *
     * Required role: ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}