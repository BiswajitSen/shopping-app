package com.shopapp.payment.repository;

import com.shopapp.payment.domain.Payment;
import com.shopapp.payment.domain.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByIdAndUserId(String id, String userId);

    Optional<Payment> findByOrderIdAndUserId(String orderId, String userId);

    Page<Payment> findByUserId(String userId, Pageable pageable);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    boolean existsByOrderId(String orderId);
}
