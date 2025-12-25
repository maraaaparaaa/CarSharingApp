package com.carsharing.backend.security;

import com.carsharing.backend.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Links User (our entity) with UserDetails (Spring Security)
 * Spring Security works with UserDetails, not directly with User
 */
@Getter
public class CustomUserDetails implements UserDetails {

    // Getter for accessing original User
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Returns roles for authority
     * Prefix "ROLE_" is needed for Spring Security
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Spring Security uses "username" but we have "email"
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // accounts do not expire
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // we don't have locking system
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // credentials do not expire
    }

    @Override
    public boolean isEnabled() {
        return true; // all users are enabled
    }

}