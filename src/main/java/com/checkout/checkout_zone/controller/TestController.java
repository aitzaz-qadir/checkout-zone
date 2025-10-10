package com.checkout.checkout_zone.controller;

// Import statements
import com.checkout.checkout_zone.entity.Role;
import com.checkout.checkout_zone.entity.User;
import com.checkout.checkout_zone.entity.Equipment;
import com.checkout.checkout_zone.entity.EquipmentCondition;
import com.checkout.checkout_zone.entity.EquipmentStatus;
import com.checkout.checkout_zone.repository.EquipmentRepository;
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

    @Autowired
    private EquipmentRepository equipmentRepository;

    // Endpoint to create a new equipment
    @GetMapping("/create-equipment")
    public String createEquipment() {
        Equipment equipment = new Equipment();
        equipment.setInternalId("LAP-001");
        equipment.setSerialNumber("SN123456789");
        equipment.setName("Dell Latitude 5420");
        equipment.setModel("Latitude 5420");
        equipment.setBrand("Dell");
        equipment.setType("Laptop");
        equipment.setCondition(EquipmentCondition.EXCELLENT);
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        equipment.setLocation("Office - Floor 3");
        equipment.setAcquisitionDate(java.time.LocalDate.of(2024, 1, 15));
        equipment.setPurchasePrice(new java.math.BigDecimal("1200.00"));
        equipment.setCurrentValue(new java.math.BigDecimal("1000.00"));
        equipment.setNotes("Standard company laptop");
        // Save the equipment to the database
        equipmentRepository.save(equipment);
        // Return a success message
        return "Equipment created successfully!";
    }

    // Endpoint to retrieve all equipment
    @GetMapping("/all-equipment")
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

}
