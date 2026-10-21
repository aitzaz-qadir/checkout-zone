package com.checkout.checkout_zone.config;

// Import statements
import com.checkout.checkout_zone.entity.*;
import com.checkout.checkout_zone.repository.EquipmentRepository;
import com.checkout.checkout_zone.service.EquipmentService;
import com.checkout.checkout_zone.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

// Annotations
@Component

/*
 * Seeds demo data on startup (all profiles).
 *
 * Creates demo users (jane.smith, bob.manager), an optional admin from
 * app.admin.* properties, and a set of demo equipment.
 * Each item is skipped if it already exists, so restarts are safe.
 */

// Data seeder class
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserService userService;
    private final EquipmentService equipmentService;
    private final EquipmentRepository equipmentRepository;
    private final String adminUsername;
    private final String adminPassword;

    // Constructor
    public DataSeeder(UserService userService,
                      EquipmentService equipmentService,
                      EquipmentRepository equipmentRepository,
                      @Value("${app.admin.username}") String adminUsername,
                      @Value("${app.admin.password}") String adminPassword) {
        this.userService = userService;
        this.equipmentService = equipmentService;
        this.equipmentRepository = equipmentRepository;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    // Runs once on startup
    @Override
    public void run(ApplicationArguments args) {
        // Demo users (public credentials)
        seedUser("jane.smith", "password123", "Jane", "Smith", "Marketing", Role.USER);
        seedUser("bob.manager", "password123", "Bob", "Manager", "IT", Role.EQUIPMENT_MANAGER);

        // Admin (private credentials, only if configured)
        if (!adminUsername.isBlank() && !adminPassword.isBlank()) {
            seedUser(adminUsername, adminPassword, "Site", "Admin", "IT", Role.ADMIN);
        }

        // Demo equipment
        seedEquipment("LAP-001", "Dell XPS 15", "Dell", "Laptop", EquipmentCondition.GOOD, EquipmentStatus.AVAILABLE, "Office - Floor 1", "1800.00");
        seedEquipment("LAP-002", "MacBook Pro 16", "Apple", "Laptop", EquipmentCondition.EXCELLENT, EquipmentStatus.AVAILABLE, "Office - Floor 2", "2500.00");
        seedEquipment("LAP-003", "ThinkPad X1 Carbon", "Lenovo", "Laptop", EquipmentCondition.FAIR, EquipmentStatus.IN_MAINTENANCE, "IT Storage", "1600.00");
        seedEquipment("CAM-001", "Canon EOS R5", "Canon", "Camera", EquipmentCondition.GOOD, EquipmentStatus.AVAILABLE, "Office - Floor 2", "3900.00");
        seedEquipment("MON-001", "Dell UltraSharp 27", "Dell", "Monitor", EquipmentCondition.NEW, EquipmentStatus.AVAILABLE, "Office - Floor 1", "650.00");
        seedEquipment("MON-002", "LG UltraFine 32", "LG", "Monitor", EquipmentCondition.GOOD, EquipmentStatus.AVAILABLE, "Office - Floor 1", "700.00");
        seedEquipment("PRJ-001", "Epson PowerLite 1795F", "Epson", "Projector", EquipmentCondition.GOOD, EquipmentStatus.AVAILABLE, "Conference Room A", "1200.00");
        seedEquipment("TAB-001", "iPad Pro 12.9", "Apple", "Tablet", EquipmentCondition.EXCELLENT, EquipmentStatus.AVAILABLE, "IT Storage", "1100.00");
    }

    // Create a user unless the username already exists
    private void seedUser(String username, String password, String firstName, String lastName, String department, Role role) {
        if (userService.getUserByUsername(username).isPresent()) return;
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(username + "@company.com");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setDepartment(department);
        user.setRole(role);
        userService.createUser(user);
        log.info("Seeded user {} ({})", username, role);
    }

    // Create equipment unless the internal ID already exists
    private void seedEquipment(String internalId, String name, String brand, String type,
                               EquipmentCondition condition, EquipmentStatus status, String location, String price) {
        if (equipmentRepository.existsByInternalId(internalId)) return;
        Equipment equipment = new Equipment();
        equipment.setInternalId(internalId);
        equipment.setName(name);
        equipment.setModel(name);
        equipment.setBrand(brand);
        equipment.setType(type);
        equipment.setCondition(condition);
        equipment.setStatus(status);
        equipment.setLocation(location);
        equipment.setPurchasePrice(new BigDecimal(price));
        equipment.setCurrentValue(new BigDecimal(price));
        equipmentService.createEquipment(equipment);
        log.info("Seeded equipment {} ({})", internalId, name);
    }
}
