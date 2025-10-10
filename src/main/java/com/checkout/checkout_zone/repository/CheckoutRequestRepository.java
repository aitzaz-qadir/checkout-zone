package com.checkout.checkout_zone.repository;

// Import statements
import com.checkout.checkout_zone.entity.CheckoutRequest;
import com.checkout.checkout_zone.entity.RequestStatus;
import com.checkout.checkout_zone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// Repository annotation
@Repository

// Interface definition
public interface CheckoutRequestRepository extends JpaRepository<CheckoutRequest, Long> {
    // Custom query methods
    List<CheckoutRequest> findByRequestedBy(User user);
    List<CheckoutRequest> findByStatus(RequestStatus status);
    List<CheckoutRequest> findByRequestedByAndStatus(User user, RequestStatus status);
    List<CheckoutRequest> findByApprovedBy(User approver);
}
