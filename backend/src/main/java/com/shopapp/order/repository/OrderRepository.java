package com.shopapp.order.repository;

import com.shopapp.order.domain.Order;
import com.shopapp.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    Page<Order> findByUserId(String userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(String id, String userId);

    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    // Find orders that contain items from a specific vendor
    @Query("{ 'items.vendorId': ?0 }")
    Page<Order> findByVendorId(String vendorId, Pageable pageable);
    
    // Find orders by vendor and status
    @Query("{ 'items.vendorId': ?0, 'status': ?1 }")
    Page<Order> findByVendorIdAndStatus(String vendorId, OrderStatus status, Pageable pageable);
    
    // Find specific order containing vendor's items
    @Query("{ '_id': ?0, 'items.vendorId': ?1 }")
    Optional<Order> findByIdAndVendorId(String orderId, String vendorId);
}
