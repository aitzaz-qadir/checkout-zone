package com.checkout.checkout_zone.controller;

// Import statements
import com.checkout.checkout_zone.entity.Role;
import com.checkout.checkout_zone.entity.User;
import com.checkout.checkout_zone.service.UserService;
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

    private final UserService userService;

    // Constructor
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Check credentials and return user details
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

    // Register a new user (always as a regular USER)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Copy only allowed fields; id, role and active are set by the server
            User user = new User();
            user.setUsername(registerRequest.username);
            user.setPassword(registerRequest.password);
            user.setEmail(registerRequest.email);
            user.setFirstName(registerRequest.firstName);
            user.setLastName(registerRequest.lastName);
            user.setDepartment(registerRequest.department);
            user.setEmployeeId(registerRequest.employeeId);
            user.setRole(Role.USER);
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

    // Inner class for register request
    public static class RegisterRequest {
        public String username;
        public String password;
        public String email;
        public String firstName;
        public String lastName;
        public String department;
        public String employeeId;
    }

}
