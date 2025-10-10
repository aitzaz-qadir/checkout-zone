package com.checkout.checkout_zone.repository;

// Import statements
import com.checkout.checkout_zone.entity.CheckoutRecord;
import com.checkout.checkout_zone.entity.Equipment;
import com.checkout.checkout_zone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// Repository annotation
@Repository

// Interface definition
public interface CheckoutRecordRepository extends JpaRepository<CheckoutRecord, Long> {
    // Custom query methods
    List<CheckoutRecord> findByUser(User user);
    List<CheckoutRecord> findByEquipment(Equipment equipment);
    List<CheckoutRecord> findByUserAndActualReturnDateIsNull(User user);  // Currently checked out by user
    List<CheckoutRecord> findByEquipmentAndActualReturnDateIsNull(Equipment equipment);  // Currently checked out
    List<CheckoutRecord> findByActualReturnDateIsNull();  // All currently checked out items
}
