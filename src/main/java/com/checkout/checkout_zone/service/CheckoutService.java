package com.checkout.checkout_zone.service;

// Import statements
import com.checkout.checkout_zone.entity.*;
import com.checkout.checkout_zone.repository.CheckoutRecordRepository;
import com.checkout.checkout_zone.repository.CheckoutRequestRepository;
import com.checkout.checkout_zone.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Service annotation
@Service

// Service class for checkout management
public class CheckoutService {

    // Autowired repositories
    @Autowired
    private CheckoutRequestRepository checkoutRequestRepository;
    @Autowired
    private CheckoutRecordRepository checkoutRecordRepository;
    @Autowired
    private EquipmentRepository equipmentRepository;

    // Create a new checkout request
    public CheckoutRequest createCheckoutRequest(User user, Set<Equipment> equipmentItems, String purpose, LocalDate neededByDate) {
        // Validate that equipment is available
        for (Equipment equipment : equipmentItems) {
            if (equipment.getStatus() != EquipmentStatus.AVAILABLE) {
                throw new IllegalArgumentException("Equipment " + equipment.getName() + " is not available");
            }
        }
        CheckoutRequest request = new CheckoutRequest();
        request.setRequestedBy(user);
        request.setEquipmentItems(equipmentItems);
        request.setPurpose(purpose);
        request.setNeededByDate(neededByDate);
        request.setStatus(RequestStatus.PENDING);
        return checkoutRequestRepository.save(request);
    }

    // Get all checkout requests
    public List<CheckoutRequest> getAllCheckoutRequests() {
        return checkoutRequestRepository.findAll();
    }

    // Get pending requests
    public List<CheckoutRequest> getPendingRequests() {
        return checkoutRequestRepository.findByStatus(RequestStatus.PENDING);
    }

    // Get requests by user
    public List<CheckoutRequest> getRequestsByUser(User user) {
        return checkoutRequestRepository.findByRequestedBy(user);
    }

    // Approve a checkout request
    @Transactional
    public CheckoutRequest approveRequest(Long requestId, User approver, String notes) {
        CheckoutRequest request = checkoutRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be approved");
        }
        // Check if equipment is still available
        for (Equipment equipment : request.getEquipmentItems()) {
            Equipment current = equipmentRepository.findById(equipment.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Equipment not found"));
            if (current.getStatus() != EquipmentStatus.AVAILABLE) {
                throw new IllegalArgumentException("Equipment " + current.getName() + " is no longer available");
            }
        }
        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedBy(approver);
        request.setApprovalDate(LocalDateTime.now());
        request.setApprovalNotes(notes);
        return checkoutRequestRepository.save(request);
    }

    // Reject a checkout request
    public CheckoutRequest rejectRequest(Long requestId, User approver, String notes) {
        CheckoutRequest request = checkoutRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + requestId));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be rejected");
        }
        request.setStatus(RequestStatus.REJECTED);
        request.setApprovedBy(approver);
        request.setApprovalDate(LocalDateTime.now());
        request.setApprovalNotes(notes);
        return checkoutRequestRepository.save(request);
    }

    // Actually hand out equipment (create checkout records)
    @Transactional
    public List<CheckoutRecord> fulfillCheckoutRequest(Long requestId, User manager, LocalDate expectedReturnDate) {
        CheckoutRequest request = checkoutRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved requests can be fulfilled");
        }
        List<CheckoutRecord> records = new java.util.ArrayList<>();
        for (Equipment equipment : request.getEquipmentItems()) {
            // Create checkout record
            CheckoutRecord record = new CheckoutRecord();
            record.setUser(request.getRequestedBy());
            record.setEquipment(equipment);
            record.setCheckoutRequest(request);
            record.setCheckoutDate(LocalDate.now());
            record.setExpectedReturnDate(expectedReturnDate);
            record.setConditionAtCheckout(equipment.getCondition());
            record.setCheckedOutByManager(manager);
            checkoutRecordRepository.save(record);
            records.add(record);
            // Update equipment status
            equipment.setStatus(EquipmentStatus.CHECKED_OUT);
            equipmentRepository.save(equipment);
        }
        // Mark request as completed
        request.setStatus(RequestStatus.COMPLETED);
        checkoutRequestRepository.save(request);
        return records;
    }

    // Return equipment
    @Transactional
    public CheckoutRecord returnEquipment(Long recordId, User manager, EquipmentCondition condition, String notes) {
        CheckoutRecord record = checkoutRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Checkout record not found with id: " + recordId));
        if (record.getActualReturnDate() != null) {
            throw new IllegalArgumentException("This equipment has already been returned");
        }
        // Update record
        record.setActualReturnDate(LocalDate.now());
        record.setConditionAtReturn(condition);
        record.setReturnNotes(notes);
        record.setReceivedByManager(manager);
        checkoutRecordRepository.save(record);
        // Update equipment
        Equipment equipment = record.getEquipment();
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        equipment.setCondition(condition);
        equipmentRepository.save(equipment);
        return record;
    }

    // Get all currently checked out items
    public List<CheckoutRecord> getCurrentlyCheckedOut() {
        return checkoutRecordRepository.findByActualReturnDateIsNull();
    }

    // Get user's currently checked out items
    public List<CheckoutRecord> getUserCurrentCheckouts(User user) {
        return checkoutRecordRepository.findByUserAndActualReturnDateIsNull(user);
    }

    // Get checkout history for equipment
    public List<CheckoutRecord> getEquipmentHistory(Equipment equipment) {
        return checkoutRecordRepository.findByEquipment(equipment);
    }

}
