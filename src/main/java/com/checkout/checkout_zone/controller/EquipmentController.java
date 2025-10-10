package com.checkout.checkout_zone.controller;

// Import statements
import com.checkout.checkout_zone.entity.Equipment;
import com.checkout.checkout_zone.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

// RestController annotation
@RestController
@RequestMapping("/api/equipment")

// Controller class for equipment management
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    // Create new equipment
    @PostMapping
    public ResponseEntity<Equipment> createEquipment(@Valid @RequestBody Equipment equipment) {
        try {
            Equipment created = equipmentService.createEquipment(equipment);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Get all equipment
    @GetMapping
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        List<Equipment> equipment = equipmentService.getAllEquipment();
        return new ResponseEntity<>(equipment, HttpStatus.OK);
    }

    // Get equipment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
        return equipmentService.getEquipmentById(id)
                .map(equipment -> new ResponseEntity<>(equipment, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Get available equipment
    @GetMapping("/available")
    public ResponseEntity<List<Equipment>> getAvailableEquipment() {
        List<Equipment> equipment = equipmentService.getAvailableEquipment();
        return new ResponseEntity<>(equipment, HttpStatus.OK);
    }

    // Get equipment by type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Equipment>> getEquipmentByType(@PathVariable String type) {
        List<Equipment> equipment = equipmentService.getEquipmentByType(type);
        return new ResponseEntity<>(equipment, HttpStatus.OK);
    }

    // Update equipment
    @PutMapping("/{id}")
    public ResponseEntity<Equipment> updateEquipment(@PathVariable Long id, @Valid @RequestBody Equipment equipment) {
        try {
            Equipment updated = equipmentService.updateEquipment(id, equipment);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Delete equipment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        try {
            equipmentService.deleteEquipment(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
