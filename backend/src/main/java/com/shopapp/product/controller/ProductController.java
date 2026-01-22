package com.shopapp.product.controller;

import com.shopapp.product.dto.*;
import com.shopapp.product.service.ProductService;
import com.shopapp.shared.dto.ApiResponse;
import com.shopapp.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Public product browsing APIs")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get approved products", description = "Get all approved and visible products with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponse> products = productService.getApprovedProducts(pageable);
        PagedResponse<ProductResponse> response = PagedResponse.of(
                products.getContent(), page, size, products.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID", description = "Get approved product details by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String productId) {
        ProductResponse product = productService.getApprovedProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search approved products by keyword or category")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(keyword)
                .category(category)
                .build();
        
        Page<ProductResponse> products = productService.searchProducts(request, pageable);
        PagedResponse<ProductResponse> response = PagedResponse.of(
                products.getContent(), page, size, products.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
