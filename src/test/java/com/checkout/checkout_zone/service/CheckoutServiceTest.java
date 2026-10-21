package com.checkout.checkout_zone.service;

// Import statements
import com.checkout.checkout_zone.entity.*;
import com.checkout.checkout_zone.repository.CheckoutRecordRepository;
import com.checkout.checkout_zone.repository.CheckoutRequestRepository;
import com.checkout.checkout_zone.repository.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Annotations
@ExtendWith(MockitoExtension.class)

/*
 * Unit tests for CheckoutService business rules.
 *
 * Repositories are mocked with Mockito, so no database is needed.
 * Each test sets up a request/record in a given state, calls one
 * service method, and checks the outcome or the error thrown.
 */

// Test class
class CheckoutServiceTest {

    @Mock
    private CheckoutRequestRepository checkoutRequestRepository;
    @Mock
    private CheckoutRecordRepository checkoutRecordRepository;
    @Mock
    private EquipmentRepository equipmentRepository;
    @InjectMocks
    private CheckoutService checkoutService;

    private User jane;
    private User bob;

    // Fresh test users before each test
    @BeforeEach
    void setUp() {
        jane = new User();
        jane.setUsername("jane.smith");
        bob = new User();
        bob.setUsername("bob.manager");
    }

    // Build equipment with the given status
    private Equipment equipment(String name, EquipmentStatus status) {
        Equipment equipment = new Equipment();
        equipment.setName(name);
        equipment.setStatus(status);
        equipment.setCondition(EquipmentCondition.GOOD);
        return equipment;
    }

    // Build a request from Jane with the given status and items
    private CheckoutRequest request(RequestStatus status, Equipment... items) {
        CheckoutRequest request = new CheckoutRequest();
        request.setId(1L);
        request.setRequestedBy(jane);
        request.setStatus(status);
        request.setEquipmentItems(new HashSet<>(Set.of(items)));
        return request;
    }

    // Build an open checkout record for Jane
    private CheckoutRecord openRecord(Equipment equipment) {
        CheckoutRecord record = new CheckoutRecord();
        record.setId(5L);
        record.setUser(jane);
        record.setEquipment(equipment);
        record.setCheckoutDate(LocalDate.now().minusDays(3));
        return record;
    }

    // Creating a request: unavailable equipment is rejected
    @Test
    void createRequest_rejectsUnavailableEquipment() {
        Equipment laptop = equipment("Laptop", EquipmentStatus.CHECKED_OUT);

        assertThrows(IllegalArgumentException.class,
                () -> checkoutService.createCheckoutRequest(jane, Set.of(laptop), "Work", LocalDate.now()));
        verify(checkoutRequestRepository, never()).save(any());
    }

    // Creating a request: saved as PENDING for the requesting user
    @Test
    void createRequest_savesAsPending() {
        Equipment laptop = equipment("Laptop", EquipmentStatus.AVAILABLE);
        when(checkoutRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutRequest created = checkoutService.createCheckoutRequest(jane, Set.of(laptop), "Work", LocalDate.now());

        assertEquals(RequestStatus.PENDING, created.getStatus());
        assertEquals(jane, created.getRequestedBy());
    }

    // Approving: only pending requests can be approved
    @Test
    void approve_rejectsNonPendingRequest() {
        CheckoutRequest rejected = request(RequestStatus.REJECTED, equipment("Laptop", EquipmentStatus.AVAILABLE));
        when(checkoutRequestRepository.findById(1L)).thenReturn(Optional.of(rejected));

        assertThrows(IllegalArgumentException.class, () -> checkoutService.approveRequest(1L, bob, "OK"));
        assertEquals(RequestStatus.REJECTED, rejected.getStatus());
    }

    // Fulfilling: only approved requests can be fulfilled
    @Test
    void fulfill_rejectsUnapprovedRequest() {
        CheckoutRequest pending = request(RequestStatus.PENDING, equipment("Laptop", EquipmentStatus.AVAILABLE));
        when(checkoutRequestRepository.findById(1L)).thenReturn(Optional.of(pending));

        assertThrows(IllegalArgumentException.class,
                () -> checkoutService.fulfillCheckoutRequest(1L, bob, LocalDate.now().plusDays(7)));
        verify(checkoutRecordRepository, never()).save(any());
    }

    // Fulfilling: equipment taken since approval blocks fulfillment (double checkout fix)
    @Test
    void fulfill_rejectsUnavailableEquipment() {
        Equipment laptop = equipment("Laptop", EquipmentStatus.CHECKED_OUT);
        CheckoutRequest approved = request(RequestStatus.APPROVED, laptop);
        when(checkoutRequestRepository.findById(1L)).thenReturn(Optional.of(approved));

        assertThrows(IllegalArgumentException.class,
                () -> checkoutService.fulfillCheckoutRequest(1L, bob, LocalDate.now().plusDays(7)));
        verify(checkoutRecordRepository, never()).save(any());
        assertEquals(RequestStatus.APPROVED, approved.getStatus());
    }

    // Fulfilling: one record per item, items checked out, request completed
    @Test
    void fulfill_createsRecordsAndChecksOutEquipment() {
        Equipment laptop = equipment("Laptop", EquipmentStatus.AVAILABLE);
        Equipment camera = equipment("Camera", EquipmentStatus.AVAILABLE);
        CheckoutRequest approved = request(RequestStatus.APPROVED, laptop, camera);
        when(checkoutRequestRepository.findById(1L)).thenReturn(Optional.of(approved));
        LocalDate returnDate = LocalDate.now().plusDays(7);

        List<CheckoutRecord> records = checkoutService.fulfillCheckoutRequest(1L, bob, returnDate);

        assertEquals(2, records.size());
        assertEquals(EquipmentStatus.CHECKED_OUT, laptop.getStatus());
        assertEquals(EquipmentStatus.CHECKED_OUT, camera.getStatus());
        assertEquals(RequestStatus.COMPLETED, approved.getStatus());
        for (CheckoutRecord record : records) {
            assertEquals(jane, record.getUser());
            assertEquals(bob, record.getCheckedOutByManager());
            assertEquals(returnDate, record.getExpectedReturnDate());
            assertNull(record.getActualReturnDate());
        }
    }

    // Returning: an already returned record is rejected
    @Test
    void return_rejectsAlreadyReturned() {
        CheckoutRecord record = openRecord(equipment("Laptop", EquipmentStatus.AVAILABLE));
        record.setActualReturnDate(LocalDate.now().minusDays(1));
        when(checkoutRecordRepository.findById(5L)).thenReturn(Optional.of(record));

        assertThrows(IllegalArgumentException.class,
                () -> checkoutService.returnEquipment(5L, bob, EquipmentCondition.GOOD, "Again"));
    }

    // Returning: equipment back to AVAILABLE with the reported condition
    @Test
    void return_marksEquipmentAvailableWithNewCondition() {
        Equipment laptop = equipment("Laptop", EquipmentStatus.CHECKED_OUT);
        CheckoutRecord record = openRecord(laptop);
        when(checkoutRecordRepository.findById(5L)).thenReturn(Optional.of(record));

        CheckoutRecord returned = checkoutService.returnEquipment(5L, bob, EquipmentCondition.FAIR, "Scratched lid");

        assertEquals(LocalDate.now(), returned.getActualReturnDate());
        assertEquals(EquipmentCondition.FAIR, returned.getConditionAtReturn());
        assertEquals(bob, returned.getReceivedByManager());
        assertEquals(EquipmentStatus.AVAILABLE, laptop.getStatus());
        assertEquals(EquipmentCondition.FAIR, laptop.getCondition());
    }

}
