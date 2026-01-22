package com.shopapp.admin.controller;

import com.shopapp.admin.dto.RejectRequest;
import com.shopapp.admin.dto.VisibilityRequest;
import com.shopapp.product.dto.ProductResponse;
import com.shopapp.product.service.ProductService;
import com.shopapp.shared.dto.ApiResponse;
import com.shopapp.shared.dto.PagedResponse;
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
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Products", description = "Admin product management APIs")
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Get all products with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        PagedResponse<ProductResponse> response = PagedResponse.of(
                products.getContent(), page, size, products.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending products", description = "Get all pending products waiting for approval")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getPendingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        
        Page<ProductResponse> products = productService.getPendingProducts(pageable);
        PagedResponse<ProductResponse> response = PagedResponse.of(
                products.getContent(), page, size, products.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID", description = "Get product details by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String productId) {
        ProductResponse product = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping("/{productId}/approve")
    @Operation(summary = "Approve product", description = "Approve a pending product")
    public ResponseEntity<ApiResponse<ProductResponse>> approveProduct(@PathVariable String productId) {
        ProductResponse product = productService.approveProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Product approved successfully", product));
    }

    @PostMapping("/{productId}/reject")
    @Operation(summary = "Reject product", description = "Reject a pending product")
    public ResponseEntity<ApiResponse<ProductResponse>> rejectProduct(
            @PathVariable String productId,
            @Valid @RequestBody RejectRequest request) {
        ProductResponse product = productService.rejectProduct(productId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Product rejected", product));
    }

    @PutMapping("/{productId}/visibility")
    @Operation(summary = "Change product visibility", description = "Show or hide a product from public view")
    public ResponseEntity<ApiResponse<ProductResponse>> changeVisibility(
            @PathVariable String productId,
            @Valid @RequestBody VisibilityRequest request) {
        ProductResponse product = productService.changeProductVisibility(productId, request.getVisible());
        String message = request.getVisible() ? "Product is now visible" : "Product is now hidden";
        return ResponseEntity.ok(ApiResponse.success(message, product));
    }
}
