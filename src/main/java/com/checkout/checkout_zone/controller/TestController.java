package com.checkout.checkout_zone.controller;

// Import statements
import com.checkout.checkout_zone.entity.Role;
import com.checkout.checkout_zone.entity.User;
import com.checkout.checkout_zone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

// Annotations
@RestController
@RequestMapping("/test")

// Class Definition - A simple test controller to demonstrate user creation
public class TestController {

    @Autowired
    private UserRepository userRepository;

    // Endpoint to create a new user
    @GetMapping("/create-user")
    public String createUser(){
        // Create a new user instance
        User user = new User();
        user.setUsername("john.doe");
        user.setPassword("password123");
        user.setEmail("john.doe@company.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDepartment("IT");
        user.setEmployeeId("EMP001");
        user.setRole(Role.USER);
        user.setActive(true);
        // Save the user to the database
        userRepository.save(user);
        // Return a success message
        return "User created successfully!";
    }

    // Endpoint to retrieve all users
    @GetMapping("/all-users")
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

}
