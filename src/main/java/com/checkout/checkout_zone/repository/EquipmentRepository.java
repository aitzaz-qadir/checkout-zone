package com.checkout.checkout_zone.repository;

// Import statements
import com.checkout.checkout_zone.entity.Equipment;
import com.checkout.checkout_zone.entity.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

// Repository annotation
@Repository

// Interface definition
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    Optional<Equipment> findByInternalId(String internalId);
    Optional<Equipment> findBySerialNumber(String serialNumber);
    List<Equipment> findByStatus(EquipmentStatus status);
    List<Equipment> findByType(String type);
    List<Equipment> findByStatusAndType(EquipmentStatus status, String type);
    boolean existsByInternalId(String internalId);
}
