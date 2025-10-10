package com.checkout.checkout_zone.service;

// Import statements
import com.checkout.checkout_zone.entity.User;
import com.checkout.checkout_zone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

// Service annotation
@Service

// Service class for user management
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Create new user
    public User createUser(User user) {
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username " + user.getUsername() + " already exists");
        }
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email " + user.getEmail() + " already exists");
        }
        // Set default active status
        if (user.getActive() == null) {
            user.setActive(true);
        }
        // TODO: Hash password before saving
        return userRepository.save(user);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Get user by username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Update user
    public User updateUser(Long id, User updatedUser) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        // Update fields
        existing.setFirstName(updatedUser.getFirstName());
        existing.setLastName(updatedUser.getLastName());
        existing.setEmail(updatedUser.getEmail());
        existing.setDepartment(updatedUser.getDepartment());
        existing.setEmployeeId(updatedUser.getEmployeeId());
        existing.setRole(updatedUser.getRole());
        existing.setActive(updatedUser.getActive());
        return userRepository.save(existing);
    }

    // Deactivate user (soft delete)
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    // Delete user (hard delete)
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

}
