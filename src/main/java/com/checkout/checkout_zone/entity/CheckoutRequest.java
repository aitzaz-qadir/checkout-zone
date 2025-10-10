package com.checkout.checkout_zone.entity;

// Import statements
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// Annotations
@Entity
@Table(name = "checkout_requests")

/*
 * Checkout Request Table;
 *
 * Like a shopping cart or permission slip for borrowing equipment. This
 * table holds requests made by employees to borrow one or more pieces of
 * equipment.
 *
 * Fields:
 * - id (Primary Key)
 * - requestedBy (Foreign Key to Users)
 * - equipmentItems (Many-to-Many Foreign Key to Equipment)
 * - status (Enum: PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED)
 * - requestedDate
 * - neededByDate
 * - purpose (Reason for checkout)
 * - approvedBy (Foreign Key to Users, who approved/rejected)
 * - approvalDate
 * - approvalNotes
 * - createdAt
 * - updatedAt
 *
 * Notes:
 *
 * Also creates a join table "request_equipment" to link requests and equipment.
 * This allows each request to include multiple equipment items, and each
 * piece of equipment to be part of multiple requests over time.
 */

// Class definition
public class CheckoutRequest {

    // Creating fields for the CheckoutRequest entity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Requesting user is required")
    @ManyToOne
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    @NotEmpty(message = "At least one equipment item is required")
    @ManyToMany
    @JoinTable(
            name = "request_equipment",
            joinColumns = @JoinColumn(name = "request_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private Set<Equipment> equipmentItems = new HashSet<>();

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;
    @NotNull(message = "Requested date is required")
    @Column(nullable = false)
    private LocalDate requestedDate;
    private LocalDate neededByDate;

    // Reason for checkout
    @Size(max = 1000, message = "Purpose cannot exceed 1000 characters")
    @Column(length = 1000)
    private String purpose;

    // Manager who approved/rejected the request
    @ManyToOne
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;
    private LocalDateTime approvalDate;

    // Notes from the approver
    @Size(max = 500, message = "Approval notes cannot exceed 500 characters")
    @Column(length = 500)
    private String approvalNotes;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Run before saving new entity
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestedDate == null) {
            requestedDate = LocalDate.now();
        }
    }

    // Run before updating existing entity
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructor
    public CheckoutRequest() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public User getRequestedBy() {
        return requestedBy;
    }
    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Set<Equipment> getEquipmentItems() {
        return equipmentItems;
    }
    public void setEquipmentItems(Set<Equipment> equipmentItems) {
        this.equipmentItems = equipmentItems;
    }

    public RequestStatus getStatus() {
        return status;
    }
    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }
    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public LocalDate getNeededByDate() {
        return neededByDate;
    }
    public void setNeededByDate(LocalDate neededByDate) {
        this.neededByDate = neededByDate;
    }

    public String getPurpose() {
        return purpose;
    }
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public User getApprovedBy() {
        return approvedBy;
    }
    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }
    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getApprovalNotes() {
        return approvalNotes;
    }
    public void setApprovalNotes(String approvalNotes) {
        this.approvalNotes = approvalNotes;
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
