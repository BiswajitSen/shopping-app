package com.shopapp.product.service;

import com.shopapp.product.domain.Product;
import com.shopapp.product.domain.ProductStatus;
import com.shopapp.product.dto.CreateProductRequest;
import com.shopapp.product.dto.ProductResponse;
import com.shopapp.product.dto.ProductSearchRequest;
import com.shopapp.product.dto.UpdateProductRequest;
import com.shopapp.product.repository.ProductRepository;
import com.shopapp.shared.events.DomainEventPublisher;
import com.shopapp.shared.events.product.ProductApprovedEvent;
import com.shopapp.shared.events.product.ProductRejectedEvent;
import com.shopapp.shared.exception.BadRequestException;
import com.shopapp.shared.exception.ForbiddenException;
import com.shopapp.shared.exception.ResourceNotFoundException;
import com.shopapp.shared.interfaces.ProductModuleApi;
import com.shopapp.shared.interfaces.VendorModuleApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private VendorModuleApi vendorModuleApi;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    private VendorModuleApi.VendorDto createVendorDto(String vendorId) {
        return new VendorModuleApi.VendorDto(
                vendorId, "userId123", "Test Store", "Description", "email@test.com", "APPROVED"
        );
    }

    @Nested
    @DisplayName("Product Creation")
    class ProductCreation {

        @Test
        @DisplayName("Should create product with PENDING status")
        void shouldCreateProductWithPendingStatus() {
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("iPhone 15")
                    .category("Electronics")
                    .price(new BigDecimal("999.99"))
                    .stock(100)
                    .description("Latest iPhone")
                    .images(List.of("image1.jpg", "image2.jpg"))
                    .build();

            Product savedProduct = Product.builder()
                    .id("productId123")
                    .name("iPhone 15")
                    .category("Electronics")
                    .price(new BigDecimal("999.99"))
                    .stock(100)
                    .vendorId("vendorId123")
                    .status(ProductStatus.PENDING)
                    .visible(true)
                    .build();

            when(vendorModuleApi.getVendorIdByUserId("userId123")).thenReturn(Optional.of("vendorId123"));
            when(vendorModuleApi.findById("vendorId123")).thenReturn(Optional.of(createVendorDto("vendorId123")));
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            ProductResponse response = productService.createProduct("userId123", request);

            assertNotNull(response);
            assertEquals("productId123", response.getId());
            assertEquals("iPhone 15", response.getName());
            assertEquals(ProductStatus.PENDING, response.getStatus());
        }

        @Test
        @DisplayName("Should throw ForbiddenException for non-approved vendor")
        void shouldThrowForbiddenExceptionForNonApprovedVendor() {
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("Test Product")
                    .category("Electronics")
                    .price(BigDecimal.valueOf(99.99))
                    .stock(10)
                    .description("Test description")
                    .images(List.of("image.jpg"))
                    .build();

            when(vendorModuleApi.getVendorIdByUserId("userId123")).thenReturn(Optional.empty());

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> productService.createProduct("userId123", request));

            assertEquals("User is not an approved vendor", exception.getMessage());
        }

        @Test
        @DisplayName("Should save all product details correctly")
        void shouldSaveAllProductDetailsCorrectly() {
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("Test Product")
                    .category("Category")
                    .price(new BigDecimal("50.00"))
                    .stock(50)
                    .description("Description")
                    .images(List.of("img.jpg"))
                    .build();

            when(vendorModuleApi.getVendorIdByUserId(anyString())).thenReturn(Optional.of("vendorId"));
            when(vendorModuleApi.findById(anyString())).thenReturn(Optional.of(createVendorDto("vendorId")));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> {
                Product p = i.getArgument(0);
                p.setId("productId");
                return p;
            });

            productService.createProduct("userId", request);

            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(productCaptor.capture());

            Product saved = productCaptor.getValue();
            assertEquals("Test Product", saved.getName());
            assertEquals("Category", saved.getCategory());
            assertEquals(new BigDecimal("50.00"), saved.getPrice());
            assertEquals(50, saved.getStock());
            assertEquals(ProductStatus.PENDING, saved.getStatus());
            assertTrue(saved.isVisible());
        }
    }

    @Nested
    @DisplayName("ProductModuleApi Implementation")
    class ProductModuleApiImplementation {

        @Test
        @DisplayName("Should find product by ID")
        void shouldFindProductById() {
            Product product = Product.builder()
                    .id("productId123")
                    .name("Test Product")
                    .category("Electronics")
                    .price(new BigDecimal("100.00"))
                    .stock(50)
                    .vendorId("vendorId123")
                    .status(ProductStatus.APPROVED)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));

            Optional<ProductModuleApi.ProductDto> result = productService.findById("productId123");

            assertTrue(result.isPresent());
            assertEquals("productId123", result.get().id());
            assertEquals("Test Product", result.get().name());
        }

        @Test
        @DisplayName("Should return true for approved product")
        void shouldReturnTrueForApprovedProduct() {
            Product product = Product.builder()
                    .id("productId123")
                    .status(ProductStatus.APPROVED)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));

            assertTrue(productService.isApprovedProduct("productId123"));
        }

        @Test
        @DisplayName("Should return false for pending product")
        void shouldReturnFalseForPendingProduct() {
            Product product = Product.builder()
                    .id("productId123")
                    .status(ProductStatus.PENDING)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));

            assertFalse(productService.isApprovedProduct("productId123"));
        }

        @Test
        @DisplayName("Should return false for non-existent product")
        void shouldReturnFalseForNonExistentProduct() {
            when(productRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertFalse(productService.isApprovedProduct("nonexistent"));
        }
    }

    @Nested
    @DisplayName("Stock Management")
    class StockManagement {

        @Test
        @DisplayName("Should decrement stock when sufficient")
        void shouldDecrementStockWhenSufficient() {
            Product product = Product.builder()
                    .id("productId123")
                    .stock(10)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            boolean result = productService.decrementStock("productId123", 5);

            assertTrue(result);
            assertEquals(5, product.getStock());
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should return false when insufficient stock")
        void shouldReturnFalseWhenInsufficientStock() {
            Product product = Product.builder()
                    .id("productId123")
                    .stock(3)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));

            boolean result = productService.decrementStock("productId123", 5);

            assertFalse(result);
            assertEquals(3, product.getStock()); // Stock unchanged
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should decrement exact stock amount")
        void shouldDecrementExactStockAmount() {
            Product product = Product.builder()
                    .id("productId123")
                    .stock(5)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            boolean result = productService.decrementStock("productId123", 5);

            assertTrue(result);
            assertEquals(0, product.getStock());
        }

        @Test
        @DisplayName("Should restore stock")
        void shouldRestoreStock() {
            Product product = Product.builder()
                    .id("productId123")
                    .stock(5)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            productService.restoreStock("productId123", 3);

            assertEquals(8, product.getStock());
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when restoring stock for non-existent product")
        void shouldThrowExceptionWhenRestoringStockForNonExistentProduct() {
            when(productRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> productService.restoreStock("nonexistent", 5));
        }
    }

    @Nested
    @DisplayName("Product Update")
    class ProductUpdate {

        @Test
        @DisplayName("Should update product name")
        void shouldUpdateProductName() {
            Product product = Product.builder()
                    .id("productId123")
                    .name("Old Name")
                    .vendorId("vendorId123")
                    .build();

            UpdateProductRequest request = UpdateProductRequest.builder()
                    .name("New Name")
                    .build();

            when(vendorModuleApi.getVendorIdByUserId("userId123")).thenReturn(Optional.of("vendorId123"));
            when(productRepository.findByIdAndVendorId("productId123", "vendorId123")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(vendorModuleApi.findById(anyString())).thenReturn(Optional.of(createVendorDto("vendorId123")));

            ProductResponse response = productService.updateProduct("userId123", "productId123", request);

            assertEquals("New Name", response.getName());
        }

        @Test
        @DisplayName("Should update product price")
        void shouldUpdateProductPrice() {
            Product product = Product.builder()
                    .id("productId123")
                    .price(new BigDecimal("100.00"))
                    .vendorId("vendorId123")
                    .build();

            UpdateProductRequest request = UpdateProductRequest.builder()
                    .price(new BigDecimal("150.00"))
                    .build();

            when(vendorModuleApi.getVendorIdByUserId("userId123")).thenReturn(Optional.of("vendorId123"));
            when(productRepository.findByIdAndVendorId("productId123", "vendorId123")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(vendorModuleApi.findById(anyString())).thenReturn(Optional.of(createVendorDto("vendorId123")));

            ProductResponse response = productService.updateProduct("userId123", "productId123", request);

            assertEquals(new BigDecimal("150.00"), response.getPrice());
        }

        @Test
        @DisplayName("Should update product stock")
        void shouldUpdateProductStock() {
            Product product = Product.builder()
                    .id("productId123")
                    .stock(50)
                    .vendorId("vendorId123")
                    .build();

            UpdateProductRequest request = UpdateProductRequest.builder()
                    .stock(100)
                    .build();

            when(vendorModuleApi.getVendorIdByUserId("userId123")).thenReturn(Optional.of("vendorId123"));
            when(productRepository.findByIdAndVendorId("productId123", "vendorId123")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(vendorModuleApi.findById(anyString())).thenReturn(Optional.of(createVendorDto("vendorId123")));

            ProductResponse response = productService.updateProduct("userId123", "productId123", request);

            assertEquals(100, response.getStock());
        }

        @Test
        @DisplayName("Should throw ForbiddenException when non-vendor tries to update")
        void shouldThrowForbiddenExceptionForNonVendor() {
            UpdateProductRequest request = UpdateProductRequest.builder().name("New Name").build();

            when(vendorModuleApi.getVendorIdByUserId("userId123")).thenReturn(Optional.empty());

            assertThrows(ForbiddenException.class, 
                    () -> productService.updateProduct("userId123", "productId123", request));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent product")
        void shouldThrowExceptionForNonExistentProduct() {
            UpdateProductRequest request = UpdateProductRequest.builder().name("New Name").build();

            when(vendorModuleApi.getVendorIdByUserId("userId123")).thenReturn(Optional.of("vendorId123"));
            when(productRepository.findByIdAndVendorId("productId123", "vendorId123")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> productService.updateProduct("userId123", "productId123", request));
        }
    }

    @Nested
    @DisplayName("Product Deletion")
    class ProductDeletion {

        @Test
        @DisplayName("Should delete product by vendor")
        void shouldDeleteProductByVendor() {
            Product product = Product.builder()
                    .id("productId123")
                    .vendorId("vendorId123")
                    .build();

            when(vendorModuleApi.getVendorIdByUserId("userId123")).thenReturn(Optional.of("vendorId123"));
            when(productRepository.findByIdAndVendorId("productId123", "vendorId123")).thenReturn(Optional.of(product));

            productService.deleteProduct("userId123", "productId123");

            verify(productRepository).delete(product);
        }

        @Test
        @DisplayName("Should throw ForbiddenException when non-vendor tries to delete")
        void shouldThrowForbiddenExceptionForNonVendorDelete() {
            when(vendorModuleApi.getVendorIdByUserId("userId123")).thenReturn(Optional.empty());

            assertThrows(ForbiddenException.class, 
                    () -> productService.deleteProduct("userId123", "productId123"));
        }
    }

    @Nested
    @DisplayName("Product Approval Workflow")
    class ProductApprovalWorkflow {

        @Test
        @DisplayName("Should approve pending product")
        void shouldApprovePendingProduct() {
            Product product = Product.builder()
                    .id("productId123")
                    .vendorId("vendorId123")
                    .status(ProductStatus.PENDING)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(vendorModuleApi.findById("vendorId123")).thenReturn(Optional.of(createVendorDto("vendorId123")));

            ProductResponse response = productService.approveProduct("productId123");

            assertEquals(ProductStatus.APPROVED, response.getStatus());
            assertNull(response.getRejectionReason());
        }

        @Test
        @DisplayName("Should publish ProductApprovedEvent on approval")
        void shouldPublishProductApprovedEventOnApproval() {
            Product product = Product.builder()
                    .id("productId123")
                    .vendorId("vendorId123")
                    .status(ProductStatus.PENDING)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(vendorModuleApi.findById("vendorId123")).thenReturn(Optional.of(createVendorDto("vendorId123")));

            productService.approveProduct("productId123");

            ArgumentCaptor<ProductApprovedEvent> eventCaptor = ArgumentCaptor.forClass(ProductApprovedEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());

            assertEquals("productId123", eventCaptor.getValue().getProductId());
            assertEquals("vendorId123", eventCaptor.getValue().getVendorId());
        }

        @Test
        @DisplayName("Should throw BadRequestException when approving non-pending product")
        void shouldThrowExceptionWhenApprovingNonPendingProduct() {
            Product product = Product.builder()
                    .id("productId123")
                    .status(ProductStatus.APPROVED)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));

            BadRequestException exception = assertThrows(BadRequestException.class, 
                    () -> productService.approveProduct("productId123"));
            
            assertEquals("Only pending products can be approved", exception.getMessage());
        }

        @Test
        @DisplayName("Should reject pending product with reason")
        void shouldRejectPendingProductWithReason() {
            Product product = Product.builder()
                    .id("productId123")
                    .vendorId("vendorId123")
                    .status(ProductStatus.PENDING)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(vendorModuleApi.findById("vendorId123")).thenReturn(Optional.of(createVendorDto("vendorId123")));

            ProductResponse response = productService.rejectProduct("productId123", "Inappropriate content");

            assertEquals(ProductStatus.REJECTED, response.getStatus());
            assertEquals("Inappropriate content", response.getRejectionReason());
        }

        @Test
        @DisplayName("Should publish ProductRejectedEvent on rejection")
        void shouldPublishProductRejectedEventOnRejection() {
            Product product = Product.builder()
                    .id("productId123")
                    .vendorId("vendorId123")
                    .status(ProductStatus.PENDING)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(vendorModuleApi.findById("vendorId123")).thenReturn(Optional.of(createVendorDto("vendorId123")));

            productService.rejectProduct("productId123", "Reason");

            ArgumentCaptor<ProductRejectedEvent> eventCaptor = ArgumentCaptor.forClass(ProductRejectedEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());

            assertEquals("productId123", eventCaptor.getValue().getProductId());
            assertEquals("Reason", eventCaptor.getValue().getReason());
        }
    }

    @Nested
    @DisplayName("Product Visibility")
    class ProductVisibility {

        @Test
        @DisplayName("Should change product visibility to hidden")
        void shouldChangeProductVisibilityToHidden() {
            Product product = Product.builder()
                    .id("productId123")
                    .vendorId("vendorId123")
                    .visible(true)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(vendorModuleApi.findById("vendorId123")).thenReturn(Optional.of(createVendorDto("vendorId123")));

            ProductResponse response = productService.changeProductVisibility("productId123", false);

            assertFalse(response.isVisible());
        }

        @Test
        @DisplayName("Should change product visibility to visible")
        void shouldChangeProductVisibilityToVisible() {
            Product product = Product.builder()
                    .id("productId123")
                    .vendorId("vendorId123")
                    .visible(false)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(vendorModuleApi.findById("vendorId123")).thenReturn(Optional.of(createVendorDto("vendorId123")));

            ProductResponse response = productService.changeProductVisibility("productId123", true);

            assertTrue(response.isVisible());
        }
    }

    @Nested
    @DisplayName("Public Product Queries")
    class PublicProductQueries {

        @Test
        @DisplayName("Should get only approved and visible products")
        void shouldGetOnlyApprovedAndVisibleProducts() {
            Product product = Product.builder()
                    .id("productId123")
                    .name("Test Product")
                    .vendorId("vendorId123")
                    .status(ProductStatus.APPROVED)
                    .visible(true)
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            when(productRepository.findByStatusAndVisibleTrue(ProductStatus.APPROVED, pageable))
                    .thenReturn(productPage);
            when(vendorModuleApi.findById(anyString())).thenReturn(Optional.of(createVendorDto("vendorId123")));

            Page<ProductResponse> result = productService.getApprovedProducts(pageable);

            assertEquals(1, result.getContent().size());
            assertEquals(ProductStatus.APPROVED, result.getContent().get(0).getStatus());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for pending product when getting by ID publicly")
        void shouldThrowExceptionForPendingProductPublicAccess() {
            Product product = Product.builder()
                    .id("productId123")
                    .status(ProductStatus.PENDING)
                    .visible(true)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));

            assertThrows(ResourceNotFoundException.class, 
                    () -> productService.getApprovedProductById("productId123"));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for hidden product when getting by ID publicly")
        void shouldThrowExceptionForHiddenProductPublicAccess() {
            Product product = Product.builder()
                    .id("productId123")
                    .status(ProductStatus.APPROVED)
                    .visible(false)
                    .build();

            when(productRepository.findById("productId123")).thenReturn(Optional.of(product));

            assertThrows(ResourceNotFoundException.class, 
                    () -> productService.getApprovedProductById("productId123"));
        }
    }
}
