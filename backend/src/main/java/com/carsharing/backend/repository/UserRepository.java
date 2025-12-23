package com.carsharing.backend.repository;

import com.carsharing.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository  // interface that interacts with the DB
public interface UserRepository extends JpaRepository<User, Long> { // type of repo and id type

    // Spring generates: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // checks if the email exists
    // Spring generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);

    // built in methods :
    // - findById(Long id)
    // - findAll()
    // - save(User user)
    // - deleteById(Long id)
    // etc.
}