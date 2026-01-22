package com.shopapp.product.controller;

import com.shopapp.product.dto.*;
import com.shopapp.product.service.ProductService;
import com.shopapp.shared.dto.ApiResponse;
import com.shopapp.shared.dto.PagedResponse;
import com.shopapp.shared.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendors/me/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDOR')")
@Tag(name = "Vendor Products", description = "Vendor product management APIs")
public class VendorProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get my products", description = "Get all products for the current vendor")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getMyProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        String userId = SecurityUtils.getCurrentUserId();
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponse> products = productService.getVendorProducts(userId, pageable);
        PagedResponse<ProductResponse> response = PagedResponse.of(
                products.getContent(), page, size, products.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product (requires approved vendor status)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        ProductResponse product = productService.createProduct(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully. Pending approval.", product));
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update product", description = "Update an existing product")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        ProductResponse product = productService.updateProduct(userId, productId, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete product", description = "Delete an existing product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String productId) {
        String userId = SecurityUtils.getCurrentUserId();
        productService.deleteProduct(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }
}
