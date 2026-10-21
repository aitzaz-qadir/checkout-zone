package com.checkout.checkout_zone.repository;

// Import statements
import com.checkout.checkout_zone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Interface definition
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}