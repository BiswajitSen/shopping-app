package com.shopapp.product.service;

import com.shopapp.product.domain.Product;
import com.shopapp.product.domain.ProductStatus;
import com.shopapp.product.dto.*;
import com.shopapp.product.repository.ProductRepository;
import com.shopapp.shared.events.DomainEventPublisher;
import com.shopapp.shared.events.product.ProductApprovedEvent;
import com.shopapp.shared.events.product.ProductRejectedEvent;
import com.shopapp.shared.exception.BadRequestException;
import com.shopapp.shared.exception.ForbiddenException;
import com.shopapp.shared.exception.ResourceNotFoundException;
import com.shopapp.shared.interfaces.ProductModuleApi;
import com.shopapp.shared.interfaces.VendorModuleApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements ProductModuleApi {

    private final ProductRepository productRepository;
    private final VendorModuleApi vendorModuleApi;
    private final DomainEventPublisher eventPublisher;

    // ===== ProductModuleApi Implementation =====

    @Override
    public Optional<ProductDto> findById(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return Optional.empty();
        }

        return productRepository.findById(productId)
                .map(this::toProductDto);
    }

    @Override
    public boolean isApprovedProduct(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return false;
        }

        return productRepository.findById(productId)
                .map(Product::isApproved)
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean decrementStock(String productId, int quantity) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new BadRequestException("Product ID is required");
        }

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.hasStock(quantity)) {
            return false;
        }

        product.decrementStock(quantity);
        productRepository.save(product);
        log.info("Decremented stock for product {} by {}", productId, quantity);
        return true;
    }

    @Override
    @Transactional
    public void restoreStock(String productId, int quantity) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new BadRequestException("Product ID is required");
        }

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.incrementStock(quantity);
        productRepository.save(product);
        log.info("Restored stock for product {} by {}", productId, quantity);
    }

    // ===== Vendor Product Operations =====

    @Transactional
    public ProductResponse createProduct(String userId, CreateProductRequest request) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("User ID is required");
        }

        if (request == null) {
            throw new BadRequestException("Product request is required");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Product name is required");
        }

        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new BadRequestException("Product category is required");
        }

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Product price must be greater than 0");
        }

        if (request.getStock() == null || request.getStock() < 0) {
            throw new BadRequestException("Product stock cannot be negative");
        }

        // Get vendor ID from user ID
        String vendorId = vendorModuleApi.getVendorIdByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("User is not an approved vendor"));

        Product product = Product.builder()
                .name(request.getName().trim())
                .category(request.getCategory().trim())
                .price(request.getPrice())
                .stock(request.getStock())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .images(request.getImages() != null ? request.getImages() : List.of())
                .vendorId(vendorId)
                .status(ProductStatus.PENDING)
                .visible(true)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created with id: {} by vendor: {}", savedProduct.getId(), vendorId);

        return toProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(String userId, String productId, UpdateProductRequest request) {
        String vendorId = vendorModuleApi.getVendorIdByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("User is not an approved vendor"));

        Product product = productRepository.findByIdAndVendorId(productId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product {} updated by vendor {}", productId, vendorId);

        return toProductResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(String userId, String productId) {
        String vendorId = vendorModuleApi.getVendorIdByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("User is not an approved vendor"));

        Product product = productRepository.findByIdAndVendorId(productId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        productRepository.delete(product);
        log.info("Product {} deleted by vendor {}", productId, vendorId);
    }

    public Page<ProductResponse> getVendorProducts(String userId, Pageable pageable) {
        String vendorId = vendorModuleApi.getVendorIdByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("User is not an approved vendor"));

        return productRepository.findByVendorId(vendorId, pageable)
                .map(this::toProductResponse);
    }

    // ===== Public Product Operations =====

    public Page<ProductResponse> getApprovedProducts(Pageable pageable) {
        return productRepository.findByStatusAndVisibleTrue(ProductStatus.APPROVED, pageable)
                .map(this::toProductResponse);
    }

    public ProductResponse getApprovedProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.isApproved() || !product.isVisible()) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        return toProductResponse(product);
    }

    public Page<ProductResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            return productRepository.findApprovedByNameContaining(request.getKeyword(), pageable)
                    .map(this::toProductResponse);
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            return productRepository.findApprovedByCategory(request.getCategory(), pageable)
                    .map(this::toProductResponse);
        }
        return getApprovedProducts(pageable);
    }

    // ===== Admin Operations =====

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toProductResponse);
    }

    public Page<ProductResponse> getPendingProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.PENDING, pageable)
                .map(this::toProductResponse);
    }

    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return toProductResponse(product);
    }

    @Transactional
    public ProductResponse approveProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.isPending()) {
            throw new BadRequestException("Only pending products can be approved");
        }

        product.setStatus(ProductStatus.APPROVED);
        product.setRejectionReason(null);

        Product approvedProduct = productRepository.save(product);
        log.info("Product {} approved", productId);

        eventPublisher.publish(new ProductApprovedEvent(productId, product.getVendorId()));

        return toProductResponse(approvedProduct);
    }

    @Transactional
    public ProductResponse rejectProduct(String productId, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.isPending()) {
            throw new BadRequestException("Only pending products can be rejected");
        }

        product.setStatus(ProductStatus.REJECTED);
        product.setRejectionReason(reason);

        Product rejectedProduct = productRepository.save(product);
        log.info("Product {} rejected with reason: {}", productId, reason);

        eventPublisher.publish(new ProductRejectedEvent(productId, product.getVendorId(), reason));

        return toProductResponse(rejectedProduct);
    }

    @Transactional
    public ProductResponse changeProductVisibility(String productId, boolean visible) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.setVisible(visible);
        Product updatedProduct = productRepository.save(product);
        log.info("Product {} visibility changed to {}", productId, visible);

        return toProductResponse(updatedProduct);
    }

    // ===== Helper Methods =====

    private ProductDto toProductDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.getVendorId(),
                product.getStatus().name()
        );
    }

    private ProductResponse toProductResponse(Product product) {
        String vendorName = vendorModuleApi.findById(product.getVendorId())
                .map(VendorModuleApi.VendorDto::businessName)
                .orElse("Unknown Vendor");

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .images(product.getImages())
                .vendorId(product.getVendorId())
                .vendorName(vendorName)
                .status(product.getStatus())
                .rejectionReason(product.getRejectionReason())
                .visible(product.isVisible())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
