package com.checkout.checkout_zone.controller;

// Import statements
import com.checkout.checkout_zone.entity.*;
import com.checkout.checkout_zone.service.CheckoutService;
import com.checkout.checkout_zone.service.UserService;
import com.checkout.checkout_zone.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// RestController annotation
@RestController
@RequestMapping("/api/checkout")

// Controller class for checkout management
public class CheckoutController {

    // Autowired services
    @Autowired
    private CheckoutService checkoutService;
    @Autowired
    private UserService userService;
    @Autowired
    private EquipmentService equipmentService;

    // DTO classes for request bodies

    // Create checkout request
    @PostMapping("/request")
    public ResponseEntity<CheckoutRequest> createCheckoutRequest(@RequestBody CheckoutRequestDTO dto) {
        try {
            User user = userService.getUserById(dto.userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Set<Equipment> equipment = new HashSet<>();
            for (Long equipmentId : dto.equipmentIds) {
                Equipment eq = equipmentService.getEquipmentById(equipmentId)
                        .orElseThrow(() -> new IllegalArgumentException("Equipment not found"));
                equipment.add(eq);
            }
            CheckoutRequest request = checkoutService.createCheckoutRequest(
                    user, equipment, dto.purpose, dto.neededByDate
            );
            return new ResponseEntity<>(request, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Get all checkout requests
    @GetMapping("/requests")
    public ResponseEntity<List<CheckoutRequest>> getAllCheckoutRequests() {
        List<CheckoutRequest> requests = checkoutService.getAllCheckoutRequests();
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    // Get pending requests
    @GetMapping("/requests/pending")
    public ResponseEntity<List<CheckoutRequest>> getPendingRequests() {
        List<CheckoutRequest> requests = checkoutService.getPendingRequests();
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    // Get requests by user
    @GetMapping("/requests/user/{userId}")
    public ResponseEntity<List<CheckoutRequest>> getRequestsByUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<CheckoutRequest> requests = checkoutService.getRequestsByUser(user);
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    // Approve request
    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<CheckoutRequest> approveRequest(
            @PathVariable Long requestId,
            @RequestBody ApprovalDTO dto) {
        try {
            User approver = userService.getUserById(dto.approverId)
                    .orElseThrow(() -> new IllegalArgumentException("Approver not found"));

            CheckoutRequest approved = checkoutService.approveRequest(requestId, approver, dto.notes);
            return new ResponseEntity<>(approved, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Reject request
    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<CheckoutRequest> rejectRequest(
            @PathVariable Long requestId,
            @RequestBody ApprovalDTO dto) {
        try {
            User approver = userService.getUserById(dto.approverId)
                    .orElseThrow(() -> new IllegalArgumentException("Approver not found"));

            CheckoutRequest rejected = checkoutService.rejectRequest(requestId, approver, dto.notes);
            return new ResponseEntity<>(rejected, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Fulfill request (hand out equipment)
    @PostMapping("/requests/{requestId}/fulfill")
    public ResponseEntity<List<CheckoutRecord>> fulfillRequest(
            @PathVariable Long requestId,
            @RequestBody FulfillmentDTO dto) {
        try {
            User manager = userService.getUserById(dto.managerId)
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found"));

            List<CheckoutRecord> records = checkoutService.fulfillCheckoutRequest(
                    requestId, manager, dto.expectedReturnDate
            );
            return new ResponseEntity<>(records, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Return equipment
    @PostMapping("/records/{recordId}/return")
    public ResponseEntity<CheckoutRecord> returnEquipment(
            @PathVariable Long recordId,
            @RequestBody ReturnDTO dto) {
        try {
            User manager = userService.getUserById(dto.managerId)
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found"));

            CheckoutRecord record = checkoutService.returnEquipment(
                    recordId, manager, dto.condition, dto.notes
            );
            return new ResponseEntity<>(record, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Get currently checked out items
    @GetMapping("/records/current")
    public ResponseEntity<List<CheckoutRecord>> getCurrentlyCheckedOut() {
        List<CheckoutRecord> records = checkoutService.getCurrentlyCheckedOut();
        return new ResponseEntity<>(records, HttpStatus.OK);
    }

    // Get user's current checkouts
    @GetMapping("/records/user/{userId}/current")
    public ResponseEntity<List<CheckoutRecord>> getUserCurrentCheckouts(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<CheckoutRecord> records = checkoutService.getUserCurrentCheckouts(user);
        return new ResponseEntity<>(records, HttpStatus.OK);
    }

    // Inner DTO classes for request bodies
    public static class CheckoutRequestDTO {
        public Long userId;
        public List<Long> equipmentIds;
        public String purpose;
        public LocalDate neededByDate;
    }

    public static class ApprovalDTO {
        public Long approverId;
        public String notes;
    }

    public static class FulfillmentDTO {
        public Long managerId;
        public LocalDate expectedReturnDate;
    }

    public static class ReturnDTO {
        public Long managerId;
        public EquipmentCondition condition;
        public String notes;
    }

}
