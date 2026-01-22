package com.shopapp.vendor.repository;

import com.shopapp.vendor.domain.Vendor;
import com.shopapp.vendor.domain.VendorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends MongoRepository<Vendor, String> {

    Optional<Vendor> findByUserId(String userId);

    boolean existsByUserId(String userId);

    Page<Vendor> findByStatus(VendorStatus status, Pageable pageable);

    Page<Vendor> findAll(Pageable pageable);
}
