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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk upload products", description = "Upload products in bulk using a CSV file")
    public ResponseEntity<ApiResponse<BulkUploadResponse>> bulkUploadProducts(
            @RequestParam("file") MultipartFile file) throws IOException {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please select a CSV file to upload"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only CSV files are allowed"));
        }

        String userId = SecurityUtils.getCurrentUserId();
        String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        
        BulkUploadResponse result = productService.bulkUploadProducts(userId, csvContent);
        
        String message = String.format("Bulk upload completed: %d successful, %d failed out of %d total",
                result.getSuccessCount(), result.getFailureCount(), result.getTotalRows());
        
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    @GetMapping("/bulk-upload/template")
    @Operation(summary = "Get CSV template", description = "Get a sample CSV template for bulk upload")
    public ResponseEntity<String> getCSVTemplate() {
        String template = """
            name,category,description,price,stock,images
            "Sample Product 1",Electronics,"A great electronic device",99.99,50,https://example.com/img1.jpg;https://example.com/img2.jpg
            "Sample Product 2",Clothing,"Comfortable cotton shirt",29.99,100,https://example.com/shirt.jpg
            "Product with comma, in name",Food,"Delicious snack",5.99,200,
            """;
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=product_upload_template.csv")
                .body(template);
    }
}
