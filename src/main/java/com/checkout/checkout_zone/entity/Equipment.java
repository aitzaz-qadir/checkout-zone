package com.checkout.checkout_zone.entity;

// Import statements
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Annotations
@Entity
@Table(name = "equipment")

/*
 * Equipment Table;
 *
 * A database to store the actual details of each piece of equipment
 * available. (each laptop, camera, etc.)
 *
 * Fields:
 * - id (Primary Key)
 * - internalId (Unique)
 * - serialNumber (Unique)
 * - name
 * - model
 * - brand
 * - type (e.g., laptop, camera)
 * - condition (Enum: NEW, GOOD, FAIR, POOR, DAMAGED)
 * - status (Enum: AVAILABLE, CHECKED_OUT, UNDER_MAINTENANCE, RETIRED)
 * - location (e.g., building, room)
 * - acquisitionDate
 * - purchasePrice
 * - currentValue
 * - warrantyExpiry
 * - notes
 * - imageUrl
 * - createdAt
 * - updatedAt
 */

// Class definition
public class Equipment {

    // Creating fields for the Equipment entity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Internal ID is required")
    @Column(unique = true, nullable = false)
    private String internalId;
    private String serialNumber;
    @NotBlank(message = "Equipment name is required")
    @Column(nullable = false)
    private String name;
    private String model;
    private String brand;

    // Example; laptop, camera, monitor
    @NotBlank(message = "Equipment type is required")
    @Column(nullable = false)
    private String type;

    @NotNull(message = "Condition is required")
    @Enumerated(EnumType.STRING)
    private EquipmentCondition condition;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;

    // Example; building, room, storage area
    private String location;

    private LocalDate acquisitionDate;
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
    private BigDecimal purchasePrice;
    @DecimalMin(value = "0.0", inclusive = true, message = "Current value must be 0 or greater")
    private BigDecimal currentValue;
    private LocalDate warrantyExpiry;

    @Column(length = 1000)
    private String notes;

    // URL to an image of the equipment (skip for now)
    private String imageUrl;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Run before saving new entity
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Run before updating existing entity
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructor
    public Equipment() {}

    // Getters and Setters
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getInternalId() {
        return internalId;
    }
    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public EquipmentCondition getCondition() {
        return condition;
    }
    public void setCondition(EquipmentCondition condition) {
        this.condition = condition;
    }

    public EquipmentStatus getStatus() {
        return status;
    }
    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }
    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }
    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public LocalDate getWarrantyExpiry() {
        return warrantyExpiry;
    }
    public void setWarrantyExpiry(LocalDate warrantyExpiry) {
        this.warrantyExpiry = warrantyExpiry;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
