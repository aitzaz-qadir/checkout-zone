package com.checkout.checkout_zone.service;

// Import statements
import com.checkout.checkout_zone.entity.Equipment;
import com.checkout.checkout_zone.entity.EquipmentStatus;
import com.checkout.checkout_zone.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

// Service annotation
@Service

// Service class for equipment management
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    // Create new equipment
    public Equipment createEquipment(Equipment equipment) {
        // Check if internal ID already exists
        if (equipmentRepository.existsByInternalId(equipment.getInternalId())) {
            throw new IllegalArgumentException("Equipment with internal ID " + equipment.getInternalId() + " already exists");
        }
        // Set default status if not provided
        if (equipment.getStatus() == null) {
            equipment.setStatus(EquipmentStatus.AVAILABLE);
        }
        return equipmentRepository.save(equipment);
    }

    // Get all equipment
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    // Get equipment by ID
    public Optional<Equipment> getEquipmentById(Long id) {
        return equipmentRepository.findById(id);
    }

    // Get available equipment
    public List<Equipment> getAvailableEquipment() {
        return equipmentRepository.findByStatus(EquipmentStatus.AVAILABLE);
    }

    // Get equipment by type
    public List<Equipment> getEquipmentByType(String type) {
        return equipmentRepository.findByType(type);
    }

    // Update equipment
    public Equipment updateEquipment(Long id, Equipment updatedEquipment) {
        Equipment existing = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with id: " + id));
        // Update fields
        existing.setName(updatedEquipment.getName());
        existing.setModel(updatedEquipment.getModel());
        existing.setBrand(updatedEquipment.getBrand());
        existing.setType(updatedEquipment.getType());
        existing.setCondition(updatedEquipment.getCondition());
        existing.setStatus(updatedEquipment.getStatus());
        existing.setLocation(updatedEquipment.getLocation());
        existing.setCurrentValue(updatedEquipment.getCurrentValue());
        existing.setNotes(updatedEquipment.getNotes());
        return equipmentRepository.save(existing);
    }

    // Delete equipment
    public void deleteEquipment(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Equipment not found with id: " + id);
        }
        equipmentRepository.deleteById(id);
    }

}
