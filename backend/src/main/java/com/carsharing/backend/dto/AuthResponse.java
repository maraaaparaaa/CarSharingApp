package com.carsharing.backend.dto;

import com.carsharing.backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response after login/register
 * Backend returns: token JWT + info user
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private String email;
    private String fullName;
    private User.UserRole role;

    // Constructor convenience for UserService
    public AuthResponse(String token, User user) {
        this.token = token;
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.role = user.getRole();
    }
}