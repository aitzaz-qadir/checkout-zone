package com.checkout.checkout_zone.controller;

// Import statements
import com.checkout.checkout_zone.entity.*;
import com.checkout.checkout_zone.service.CheckoutService;
import com.checkout.checkout_zone.service.UserService;
import com.checkout.checkout_zone.service.EquipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    // Services
    private final CheckoutService checkoutService;
    private final UserService userService;
    private final EquipmentService equipmentService;

    // Constructor
    public CheckoutController(CheckoutService checkoutService,
                              UserService userService,
                              EquipmentService equipmentService) {
        this.checkoutService = checkoutService;
        this.userService = userService;
        this.equipmentService = equipmentService;
    }

    // Look up the logged-in user from the verified principal
    private User currentUser(UserDetails principal) {
        return userService.getUserByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));
    }

    // Create checkout request
    @PostMapping("/request")
    public ResponseEntity<CheckoutRequest> createCheckoutRequest(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody CheckoutRequestDTO dto) {
        try {
            User user = currentUser(principal);

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
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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

    // Get the logged-in user's requests
    @GetMapping("/requests/mine")
    public ResponseEntity<List<CheckoutRequest>> getMyRequests(@AuthenticationPrincipal UserDetails principal) {
        List<CheckoutRequest> requests = checkoutService.getRequestsByUser(currentUser(principal));
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    // Approve request
    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<CheckoutRequest> approveRequest(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long requestId,
            @RequestBody ApprovalDTO dto) {
        try {
            User approver = currentUser(principal);
            CheckoutRequest approved = checkoutService.approveRequest(requestId, approver, dto.notes);
            return new ResponseEntity<>(approved, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Reject request
    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<CheckoutRequest> rejectRequest(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long requestId,
            @RequestBody ApprovalDTO dto) {
        try {
            User approver = currentUser(principal);
            CheckoutRequest rejected = checkoutService.rejectRequest(requestId, approver, dto.notes);
            return new ResponseEntity<>(rejected, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Fulfill request (hand out equipment)
    @PostMapping("/requests/{requestId}/fulfill")
    public ResponseEntity<List<CheckoutRecord>> fulfillRequest(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long requestId,
            @RequestBody FulfillmentDTO dto) {
        try {
            User manager = currentUser(principal);
            List<CheckoutRecord> records = checkoutService.fulfillCheckoutRequest(
                    requestId, manager, dto.expectedReturnDate
            );
            return new ResponseEntity<>(records, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Return equipment
    @PostMapping("/records/{recordId}/return")
    public ResponseEntity<CheckoutRecord> returnEquipment(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long recordId,
            @RequestBody ReturnDTO dto) {
        try {
            User manager = currentUser(principal);
            CheckoutRecord record = checkoutService.returnEquipment(
                    recordId, manager, dto.condition, dto.notes
            );
            return new ResponseEntity<>(record, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Get currently checked out items
    @GetMapping("/records/current")
    public ResponseEntity<List<CheckoutRecord>> getCurrentlyCheckedOut() {
        List<CheckoutRecord> records = checkoutService.getCurrentlyCheckedOut();
        return new ResponseEntity<>(records, HttpStatus.OK);
    }

    // Get the logged-in user's current checkouts
    @GetMapping("/records/mine/current")
    public ResponseEntity<List<CheckoutRecord>> getMyCurrentCheckouts(@AuthenticationPrincipal UserDetails principal) {
        List<CheckoutRecord> records = checkoutService.getUserCurrentCheckouts(currentUser(principal));
        return new ResponseEntity<>(records, HttpStatus.OK);
    }

    // Inner DTO classes for request bodies
    public static class CheckoutRequestDTO {
        public List<Long> equipmentIds;
        public String purpose;
        public LocalDate neededByDate;
    }

    public static class ApprovalDTO {
        public String notes;
    }

    public static class FulfillmentDTO {
        public LocalDate expectedReturnDate;
    }

    public static class ReturnDTO {
        public EquipmentCondition condition;
        public String notes;
    }

}
