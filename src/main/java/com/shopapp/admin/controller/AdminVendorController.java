package com.shopapp.admin.controller;

import com.shopapp.admin.dto.RejectRequest;
import com.shopapp.shared.dto.ApiResponse;
import com.shopapp.shared.dto.PagedResponse;
import com.shopapp.vendor.dto.VendorResponse;
import com.shopapp.vendor.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/vendors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Vendors", description = "Admin vendor management APIs")
public class AdminVendorController {

    private final VendorService vendorService;

    @GetMapping
    @Operation(summary = "Get all vendors", description = "Get all vendors with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<VendorResponse>>> getAllVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<VendorResponse> vendors = vendorService.getAllVendors(pageable);
        PagedResponse<VendorResponse> response = PagedResponse.of(
                vendors.getContent(), page, size, vendors.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending vendors", description = "Get all pending vendors waiting for approval")
    public ResponseEntity<ApiResponse<PagedResponse<VendorResponse>>> getPendingVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        
        Page<VendorResponse> vendors = vendorService.getPendingVendors(pageable);
        PagedResponse<VendorResponse> response = PagedResponse.of(
                vendors.getContent(), page, size, vendors.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{vendorId}")
    @Operation(summary = "Get vendor by ID", description = "Get vendor details by ID")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendorById(@PathVariable String vendorId) {
        VendorResponse vendor = vendorService.getVendorById(vendorId);
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }

    @PostMapping("/{vendorId}/approve")
    @Operation(summary = "Approve vendor", description = "Approve a pending vendor registration")
    public ResponseEntity<ApiResponse<VendorResponse>> approveVendor(@PathVariable String vendorId) {
        VendorResponse vendor = vendorService.approveVendor(vendorId);
        return ResponseEntity.ok(ApiResponse.success("Vendor approved successfully", vendor));
    }

    @PostMapping("/{vendorId}/reject")
    @Operation(summary = "Reject vendor", description = "Reject a pending vendor registration")
    public ResponseEntity<ApiResponse<VendorResponse>> rejectVendor(
            @PathVariable String vendorId,
            @Valid @RequestBody RejectRequest request) {
        VendorResponse vendor = vendorService.rejectVendor(vendorId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Vendor rejected", vendor));
    }
}
