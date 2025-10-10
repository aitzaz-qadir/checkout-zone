package com.checkout.checkout_zone.controller;

// Import statements
import com.checkout.checkout_zone.entity.*;
import com.checkout.checkout_zone.repository.*;
import com.checkout.checkout_zone.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Annotations
@RestController
@RequestMapping("/test")

/*
 * Basic controller to demonstrate the workflow of the application.
 * This is for testing purposes only and not intended for production use.
 * It creates demo users, equipment, and simulates the checkout workflow step-by-step.
 * Each step can be triggered via a GET request to the corresponding endpoint.
 *
 * Endpoints:
 * - /test/setup-demo: Creates demo users and equipment.
 * - /test/workflow-step1-request: Jane requests equipment.
 * - /test/workflow-step2-approve: Bob approves the request.
 * - /test/workflow-step3-fulfill: Bob fulfills the request and hands out equipment.
 * - /test/workflow-step4-return: Jane returns the equipment.
 * - /test/view-all-data: View all users, equipment, requests, and checkout records.
 * - /test/clear-demo: Instructions to clear demo data (manual).
 */

// Controller class
public class TestController {

    @Autowired
    private UserService userService;
    @Autowired
    private EquipmentService equipmentService;
    @Autowired
    private CheckoutService checkoutService;
    @Autowired
    private EquipmentRepository equipmentRepository;

    @GetMapping("/setup-demo")
    public String setupDemo() {
        try {
            // Check if demo users already exist
            if (userService.getUserByUsername("jane.smith").isPresent()) {
                return "Demo data already exists! Use /test/clear-demo to reset first.";
            }

            // Create a regular user
            User regularUser = new User();
            regularUser.setUsername("jane.smith");
            regularUser.setPassword("password123");
            regularUser.setEmail("jane.smith@company.com");
            regularUser.setFirstName("Jane");
            regularUser.setLastName("Smith");
            regularUser.setDepartment("Marketing");
            regularUser.setEmployeeId("EMP002");
            regularUser.setRole(Role.USER);
            regularUser.setActive(true);
            userService.createUser(regularUser);

            // Create a manager
            User manager = new User();
            manager.setUsername("bob.manager");
            manager.setPassword("password123");
            manager.setEmail("bob.manager@company.com");
            manager.setFirstName("Bob");
            manager.setLastName("Manager");
            manager.setDepartment("IT");
            manager.setEmployeeId("EMP003");
            manager.setRole(Role.EQUIPMENT_MANAGER);
            manager.setActive(true);
            userService.createUser(manager);

            // Check if equipment already exists
            if (equipmentRepository.existsByInternalId("LAP-002")) {
                return "Demo equipment already exists! Use /test/clear-demo to reset first.";
            }

            // Create equipment
            Equipment laptop = new Equipment();
            laptop.setInternalId("LAP-002");
            laptop.setSerialNumber("SN987654321");
            laptop.setName("MacBook Pro 16");
            laptop.setModel("MacBook Pro 16-inch 2024");
            laptop.setBrand("Apple");
            laptop.setType("Laptop");
            laptop.setCondition(EquipmentCondition.EXCELLENT);
            laptop.setStatus(EquipmentStatus.AVAILABLE);
            laptop.setLocation("Office - Floor 2");
            laptop.setAcquisitionDate(LocalDate.of(2024, 3, 1));
            laptop.setPurchasePrice(new BigDecimal("2500.00"));
            laptop.setCurrentValue(new BigDecimal("2300.00"));
            laptop.setNotes("High-performance laptop for designers");
            equipmentService.createEquipment(laptop);

            Equipment camera = new Equipment();
            camera.setInternalId("CAM-001");
            camera.setSerialNumber("CAM123456");
            camera.setName("Canon EOS R5");
            camera.setModel("EOS R5");
            camera.setBrand("Canon");
            camera.setType("Camera");
            camera.setCondition(EquipmentCondition.GOOD);
            camera.setStatus(EquipmentStatus.AVAILABLE);
            camera.setLocation("Office - Floor 2");
            camera.setAcquisitionDate(LocalDate.of(2023, 6, 15));
            camera.setPurchasePrice(new BigDecimal("3900.00"));
            camera.setCurrentValue(new BigDecimal("3200.00"));
            camera.setNotes("Professional camera for marketing events");
            equipmentService.createEquipment(camera);

            return "Demo data created! Users: jane.smith (USER), bob.manager (MANAGER). Equipment: MacBook Pro (LAP-002), Canon Camera (CAM-001)";
        } catch (Exception e) {
            return "Error creating demo data: " + e.getMessage();
        }
    }

    @GetMapping("/workflow-step1-request")
    public String createRequest() {
        try {
            // Jane requests a laptop and camera
            User jane = userService.getUserByUsername("jane.smith")
                    .orElseThrow(() -> new RuntimeException("Jane not found. Run /test/setup-demo first!"));

            Equipment laptop = equipmentRepository.findByInternalId("LAP-002")
                    .orElseThrow(() -> new RuntimeException("Laptop not found. Run /test/setup-demo first!"));

            Equipment camera = equipmentRepository.findByInternalId("CAM-001")
                    .orElseThrow(() -> new RuntimeException("Camera not found. Run /test/setup-demo first!"));

            Set<Equipment> items = new HashSet<>();
            items.add(laptop);
            items.add(camera);

            CheckoutRequest request = checkoutService.createCheckoutRequest(
                    jane,
                    items,
                    "Need equipment for client presentation and product photoshoot",
                    LocalDate.now().plusDays(7)
            );

            return "Checkout request created! Request ID: " + request.getId() + " - Status: " + request.getStatus();
        } catch (Exception e) {
            return "Error creating request: " + e.getMessage();
        }
    }

    @GetMapping("/workflow-step2-approve")
    public String approveRequest() {
        try {
            // Bob (manager) approves the request
            User bob = userService.getUserByUsername("bob.manager")
                    .orElseThrow(() -> new RuntimeException("Bob not found. Run /test/setup-demo first!"));

            List<CheckoutRequest> pending = checkoutService.getPendingRequests();

            if (pending.isEmpty()) {
                return "No pending requests to approve! Run /test/workflow-step1-request first.";
            }

            CheckoutRequest request = pending.get(0);
            CheckoutRequest approved = checkoutService.approveRequest(
                    request.getId(),
                    bob,
                    "Approved for client presentation"
            );

            return "Request approved! Request ID: " + approved.getId() + " - Status: " + approved.getStatus();
        } catch (Exception e) {
            return "Error approving request: " + e.getMessage();
        }
    }

    @GetMapping("/workflow-step3-fulfill")
    public String fulfillRequest() {
        try {
            // Bob hands out the equipment
            User bob = userService.getUserByUsername("bob.manager")
                    .orElseThrow(() -> new RuntimeException("Bob not found. Run /test/setup-demo first!"));

            List<CheckoutRequest> approved = checkoutService.getAllCheckoutRequests().stream()
                    .filter(r -> r.getStatus() == RequestStatus.APPROVED)
                    .toList();

            if (approved.isEmpty()) {
                return "No approved requests to fulfill! Run /test/workflow-step2-approve first.";
            }

            CheckoutRequest request = approved.get(0);
            List<CheckoutRecord> records = checkoutService.fulfillCheckoutRequest(
                    request.getId(),
                    bob,
                    LocalDate.now().plusDays(7)
            );

            return "Equipment handed out! Created " + records.size() + " checkout records. Request status: COMPLETED";
        } catch (Exception e) {
            return "Error fulfilling request: " + e.getMessage();
        }
    }

    @GetMapping("/workflow-step4-return")
    public String returnEquipment() {
        try {
            // Jane returns the equipment
            User bob = userService.getUserByUsername("bob.manager")
                    .orElseThrow(() -> new RuntimeException("Bob not found. Run /test/setup-demo first!"));

            List<CheckoutRecord> currentCheckouts = checkoutService.getCurrentlyCheckedOut();

            if (currentCheckouts.isEmpty()) {
                return "No equipment currently checked out! Run /test/workflow-step3-fulfill first.";
            }

            CheckoutRecord record = currentCheckouts.get(0);
            CheckoutRecord returned = checkoutService.returnEquipment(
                    record.getId(),
                    bob,
                    EquipmentCondition.GOOD,
                    "Equipment returned in good condition. Minor wear on laptop case."
            );

            return "Equipment returned! Record ID: " + returned.getId() + " - Equipment is now AVAILABLE again";
        } catch (Exception e) {
            return "Error returning equipment: " + e.getMessage();
        }
    }

    @GetMapping("/view-all-data")
    public String viewAllData() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== USERS ===\n");
        userService.getAllUsers().forEach(u ->
                sb.append(u.getUsername()).append(" (").append(u.getRole()).append(")\n")
        );

        sb.append("\n=== EQUIPMENT ===\n");
        equipmentService.getAllEquipment().forEach(e ->
                sb.append(e.getName()).append(" - ").append(e.getStatus()).append(" - Internal ID: ").append(e.getInternalId()).append("\n")
        );

        sb.append("\n=== CHECKOUT REQUESTS ===\n");
        checkoutService.getAllCheckoutRequests().forEach(r ->
                sb.append("Request #").append(r.getId()).append(" - ")
                        .append(r.getStatus()).append(" - ")
                        .append(r.getEquipmentItems().size()).append(" items\n")
        );

        sb.append("\n=== CHECKOUT RECORDS ===\n");
        List<CheckoutRecord> allRecords = checkoutService.getCurrentlyCheckedOut();
        if (allRecords.isEmpty()) {
            sb.append("(No items currently checked out)\n");
        } else {
            allRecords.forEach(r ->
                    sb.append(r.getEquipment().getName()).append(" - ")
                            .append(r.getUser().getUsername()).append("\n")
            );
        }

        return sb.toString().replace("\n", "<br>");
    }

    @GetMapping("/clear-demo")
    public String clearDemo() {
        // This is just for testing - helps you start fresh
        return "To clear demo data, restart your app and database. Or manually delete from pgAdmin.";
    }
}