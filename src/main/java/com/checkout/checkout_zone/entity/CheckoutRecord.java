package com.checkout.checkout_zone.entity;

// Import statements
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Annotations
@Entity
@Table(name = "checkout_records")

/*
 * Checkout Record Table;
 *
 * Logs each instance of equipment being checked out and returned.
 * Each record links to the user who borrowed it, the equipment item,
 * and the original request. It tracks dates, condition, and who
 * managed the process.
 *
 * Fields:
 * - id (Primary Key)
 * - user (Foreign Key to Users)
 * - equipment (Foreign Key to Equipment)
 * - checkoutRequest (Foreign Key to CheckoutRequests)
 * - checkoutDate
 * - expectedReturnDate
 * - actualReturnDate
 * - conditionAtCheckout (Enum: NEW, GOOD, FAIR, POOR, DAMAGED)
 * - conditionAtReturn (Enum: NEW, GOOD, FAIR, POOR, DAMAGED)
 * - returnNotes
 * - checkedOutByManager (Foreign Key to Users)
 * - receivedByManager (Foreign Key to Users)
 * - createdAt
 * - updatedAt
 */

// Class definition
public class CheckoutRecord {

    // Creating fields for the CheckoutRecord entity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who borrowed the equipment
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // What equipment was borrowed
    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    // Link to the original request
    @ManyToOne
    @JoinColumn(name = "checkout_request_id")
    private CheckoutRequest checkoutRequest;

    @Column(nullable = false)
    private LocalDate checkoutDate;
    private LocalDate expectedReturnDate;
    // Null until returned
    private LocalDate actualReturnDate;

    @Enumerated(EnumType.STRING)
    private EquipmentCondition conditionAtCheckout;
    // Null until returned
    @Enumerated(EnumType.STRING)
    private EquipmentCondition conditionAtReturn;

    // Notes when checking out
    @Column(length = 1000)
    private String returnNotes;

    // Which manager handled the checkout and return
    @ManyToOne
    @JoinColumn(name = "checked_out_by_manager_id")
    private User checkedOutByManager;

    // Which manager received it back
    @ManyToOne
    @JoinColumn(name = "received_by_manager_id")
    private User receivedByManager;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Run before saving new entity
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (checkoutDate == null) {
            checkoutDate = LocalDate.now();
        }
    }

    // Run before updating existing entity
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructor
    public CheckoutRecord() {}

    // Getters and Setters
    public EquipmentCondition getConditionAtCheckout() {
        return conditionAtCheckout;
    }
    public void setConditionAtCheckout(EquipmentCondition conditionAtCheckout) {
        this.conditionAtCheckout = conditionAtCheckout;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Equipment getEquipment() {
        return equipment;
    }
    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public CheckoutRequest getCheckoutRequest() {
        return checkoutRequest;
    }
    public void setCheckoutRequest(CheckoutRequest checkoutRequest) {
        this.checkoutRequest = checkoutRequest;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }
    public void setCheckoutDate(LocalDate checkoutDate) {
        this.checkoutDate = checkoutDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }
    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public EquipmentCondition getConditionAtReturn() {
        return conditionAtReturn;
    }
    public void setConditionAtReturn(EquipmentCondition conditionAtReturn) {
        this.conditionAtReturn = conditionAtReturn;
    }

    public String getReturnNotes() {
        return returnNotes;
    }
    public void setReturnNotes(String returnNotes) {
        this.returnNotes = returnNotes;
    }

    public User getCheckedOutByManager() {
        return checkedOutByManager;
    }
    public void setCheckedOutByManager(User checkedOutByManager) {
        this.checkedOutByManager = checkedOutByManager;
    }

    public User getReceivedByManager() {
        return receivedByManager;
    }
    public void setReceivedByManager(User receivedByManager) {
        this.receivedByManager = receivedByManager;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}