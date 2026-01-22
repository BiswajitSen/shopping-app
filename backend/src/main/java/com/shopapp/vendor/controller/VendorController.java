package com.shopapp.vendor.controller;

import com.shopapp.shared.dto.ApiResponse;
import com.shopapp.shared.security.SecurityUtils;
import com.shopapp.vendor.dto.UpdateVendorRequest;
import com.shopapp.vendor.dto.VendorRegistrationRequest;
import com.shopapp.vendor.dto.VendorResponse;
import com.shopapp.vendor.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendors", description = "Vendor management APIs")
public class VendorController {

    private final VendorService vendorService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Register as vendor", description = "Submit vendor registration request")
    public ResponseEntity<ApiResponse<VendorResponse>> registerAsVendor(
            @Valid @RequestBody VendorRegistrationRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        VendorResponse response = vendorService.registerAsVendor(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor registration submitted successfully", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('VENDOR') or hasRole('USER')")
    @Operation(summary = "Get vendor profile", description = "Get current user's vendor profile")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendorProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        VendorResponse response = vendorService.getVendorProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Update vendor profile", description = "Update current user's vendor profile")
    public ResponseEntity<ApiResponse<VendorResponse>> updateVendorProfile(
            @Valid @RequestBody UpdateVendorRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        VendorResponse response = vendorService.updateVendorProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Vendor profile updated successfully", response));
    }

    @GetMapping("/{vendorId}")
    @Operation(summary = "Get vendor by ID", description = "Get vendor details by vendor ID (public)")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendorById(@PathVariable String vendorId) {
        VendorResponse response = vendorService.getVendorById(vendorId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
