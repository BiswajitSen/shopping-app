package com.shopapp.product.repository;

import com.shopapp.product.domain.Product;
import com.shopapp.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Page<Product> findByVendorId(String vendorId, Pageable pageable);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByStatusAndVisibleTrue(ProductStatus status, Pageable pageable);

    Page<Product> findByVendorIdAndStatus(String vendorId, ProductStatus status, Pageable pageable);

    @Query("{ 'status': 'APPROVED', 'visible': true, 'category': ?0 }")
    Page<Product> findApprovedByCategory(String category, Pageable pageable);

    @Query("{ 'status': 'APPROVED', 'visible': true, 'name': { $regex: ?0, $options: 'i' } }")
    Page<Product> findApprovedByNameContaining(String keyword, Pageable pageable);

    Optional<Product> findByIdAndVendorId(String id, String vendorId);

    List<Product> findByIdIn(List<String> ids);

    long countByVendorIdAndStatus(String vendorId, ProductStatus status);
}
