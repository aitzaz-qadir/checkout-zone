package com.checkout.checkout_zone.controller;

// Import statements
import com.checkout.checkout_zone.entity.User;
import com.checkout.checkout_zone.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Rest controller annotation
@RestController
@RequestMapping("/api/auth")

// Controller class for authentication
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userService.authenticate(
                loginRequest.username,
                loginRequest.password
        );

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Don't send password back!
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("role", user.getRole());
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User created = userService.createUser(user);

            // Don't send password back!
            Map<String, Object> response = new HashMap<>();
            response.put("id", created.getId());
            response.put("username", created.getUsername());
            response.put("email", created.getEmail());
            response.put("message", "User registered successfully");

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Inner class for login request
    public static class LoginRequest {
        public String username;
        public String password;
    }

}
