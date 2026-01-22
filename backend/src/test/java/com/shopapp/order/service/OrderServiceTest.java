package com.shopapp.order.service;

import com.shopapp.order.domain.Order;
import com.shopapp.order.domain.OrderItem;
import com.shopapp.order.domain.OrderStatus;
import com.shopapp.order.dto.*;
import com.shopapp.order.repository.OrderRepository;
import com.shopapp.shared.events.DomainEventPublisher;
import com.shopapp.shared.events.order.OrderConfirmedEvent;
import com.shopapp.shared.events.order.OrderCreatedEvent;
import com.shopapp.shared.events.payment.PaymentFailedEvent;
import com.shopapp.shared.events.payment.PaymentSuccessEvent;
import com.shopapp.shared.exception.BadRequestException;
import com.shopapp.shared.exception.ResourceNotFoundException;
import com.shopapp.shared.interfaces.OrderModuleApi;
import com.shopapp.shared.interfaces.ProductModuleApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductModuleApi productModuleApi;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest createValidOrderRequest() {
        return CreateOrderRequest.builder()
                .items(List.of(
                        OrderItemRequest.builder()
                                .productId("product1")
                                .quantity(2)
                                .build()
                ))
                .shippingAddress(ShippingAddressRequest.builder()
                        .fullName("John Doe")
                        .addressLine1("123 Main St")
                        .city("New York")
                        .state("NY")
                        .postalCode("10001")
                        .country("USA")
                        .phoneNumber("1234567890")
                        .build())
                .build();
    }

    private ProductModuleApi.ProductDto createProductDto(String id, String name, BigDecimal price, int stock) {
        return new ProductModuleApi.ProductDto(id, name, "Category", price, stock, "vendorId", "APPROVED", List.of());
    }

    @Nested
    @DisplayName("Order Creation")
    class OrderCreation {

        @Test
        @DisplayName("Should create order with single item")
        void shouldCreateOrderWithSingleItem() {
            CreateOrderRequest request = createValidOrderRequest();

            ProductModuleApi.ProductDto productDto = createProductDto("product1", "Test Product", 
                    new BigDecimal("50.00"), 10);

            Order savedOrder = Order.builder()
                    .id("order123")
                    .userId("user123")
                    .items(List.of(OrderItem.create("product1", "Test Product", "", "vendorId", 2, new BigDecimal("50.00"))))
                    .totalAmount(new BigDecimal("100.00"))
                    .status(OrderStatus.PLACED)
                    .build();

            when(productModuleApi.findById("product1")).thenReturn(Optional.of(productDto));
            when(productModuleApi.decrementStock("product1", 2)).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            OrderResponse response = orderService.createOrder("user123", request);

            assertNotNull(response);
            assertEquals("order123", response.getId());
            assertEquals(OrderStatus.PLACED, response.getStatus());
            assertEquals(new BigDecimal("100.00"), response.getTotalAmount());
            assertEquals(1, response.getItems().size());
        }

        @Test
        @DisplayName("Should create order with multiple items")
        void shouldCreateOrderWithMultipleItems() {
            CreateOrderRequest request = CreateOrderRequest.builder()
                    .items(List.of(
                            OrderItemRequest.builder().productId("product1").quantity(2).build(),
                            OrderItemRequest.builder().productId("product2").quantity(1).build()
                    ))
                    .shippingAddress(ShippingAddressRequest.builder()
                            .fullName("John Doe")
                            .addressLine1("123 Main St")
                            .city("New York")
                            .state("NY")
                            .postalCode("10001")
                            .country("USA")
                            .phoneNumber("1234567890")
                            .build())
                    .build();

            ProductModuleApi.ProductDto product1 = createProductDto("product1", "Product 1", 
                    new BigDecimal("50.00"), 10);
            ProductModuleApi.ProductDto product2 = createProductDto("product2", "Product 2", 
                    new BigDecimal("30.00"), 5);

            List<OrderItem> items = new ArrayList<>();
            items.add(OrderItem.create("product1", "Product 1", "", "vendorId", 2, new BigDecimal("50.00")));
            items.add(OrderItem.create("product2", "Product 2", "", "vendorId", 1, new BigDecimal("30.00")));

            Order savedOrder = Order.builder()
                    .id("order123")
                    .userId("user123")
                    .items(items)
                    .totalAmount(new BigDecimal("130.00"))
                    .status(OrderStatus.CREATED)
                    .build();

            when(productModuleApi.findById("product1")).thenReturn(Optional.of(product1));
            when(productModuleApi.findById("product2")).thenReturn(Optional.of(product2));
            when(productModuleApi.decrementStock(anyString(), anyInt())).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            OrderResponse response = orderService.createOrder("user123", request);

            assertEquals(2, response.getItems().size());
            assertEquals(new BigDecimal("130.00"), response.getTotalAmount());
        }

        @Test
        @DisplayName("Should publish OrderCreatedEvent after order creation")
        void shouldPublishOrderCreatedEventAfterOrderCreation() {
            CreateOrderRequest request = createValidOrderRequest();
            ProductModuleApi.ProductDto productDto = createProductDto("product1", "Test Product", 
                    new BigDecimal("50.00"), 10);

            Order savedOrder = Order.builder()
                    .id("order123")
                    .userId("user123")
                    .totalAmount(new BigDecimal("100.00"))
                    .status(OrderStatus.PLACED)
                    .items(List.of())
                    .build();

            when(productModuleApi.findById("product1")).thenReturn(Optional.of(productDto));
            when(productModuleApi.decrementStock(anyString(), anyInt())).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            orderService.createOrder("user123", request);

            ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());

            assertEquals("order123", eventCaptor.getValue().getOrderId());
            assertEquals("user123", eventCaptor.getValue().getUserId());
        }

        @Test
        @DisplayName("Should throw BadRequestException for non-existent product")
        void shouldThrowExceptionForNonExistentProduct() {
            CreateOrderRequest request = createValidOrderRequest();

            when(productModuleApi.findById("product1")).thenReturn(Optional.empty());

            BadRequestException exception = assertThrows(BadRequestException.class, 
                    () -> orderService.createOrder("user123", request));
            
            assertTrue(exception.getMessage().contains("Product not found"));
        }

        @Test
        @DisplayName("Should throw BadRequestException for non-approved product")
        void shouldThrowExceptionForNonApprovedProduct() {
            CreateOrderRequest request = createValidOrderRequest();

            ProductModuleApi.ProductDto pendingProduct = new ProductModuleApi.ProductDto(
                    "product1", "Test Product", "Category", new BigDecimal("50.00"), 10, "vendorId", "PENDING", List.of()
            );

            when(productModuleApi.findById("product1")).thenReturn(Optional.of(pendingProduct));

            BadRequestException exception = assertThrows(BadRequestException.class, 
                    () -> orderService.createOrder("user123", request));
            
            assertTrue(exception.getMessage().contains("not available"));
        }

        @Test
        @DisplayName("Should throw BadRequestException for insufficient stock")
        void shouldThrowExceptionForInsufficientStock() {
            CreateOrderRequest request = createValidOrderRequest();

            ProductModuleApi.ProductDto productDto = createProductDto("product1", "Test Product", 
                    new BigDecimal("50.00"), 10);

            when(productModuleApi.findById("product1")).thenReturn(Optional.of(productDto));
            when(productModuleApi.decrementStock("product1", 2)).thenReturn(false);

            BadRequestException exception = assertThrows(BadRequestException.class, 
                    () -> orderService.createOrder("user123", request));
            
            assertTrue(exception.getMessage().contains("Insufficient stock"));
        }

        @Test
        @DisplayName("Should calculate total amount correctly")
        void shouldCalculateTotalAmountCorrectly() {
            CreateOrderRequest request = CreateOrderRequest.builder()
                    .items(List.of(
                            OrderItemRequest.builder().productId("product1").quantity(3).build()
                    ))
                    .shippingAddress(ShippingAddressRequest.builder()
                            .fullName("John Doe")
                            .addressLine1("123 Main St")
                            .city("New York")
                            .state("NY")
                            .postalCode("10001")
                            .country("USA")
                            .phoneNumber("1234567890")
                            .build())
                    .build();

            ProductModuleApi.ProductDto productDto = createProductDto("product1", "Test Product", 
                    new BigDecimal("33.33"), 10);

            when(productModuleApi.findById("product1")).thenReturn(Optional.of(productDto));
            when(productModuleApi.decrementStock(anyString(), anyInt())).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
                Order order = i.getArgument(0);
                order.setId("order123");
                return order;
            });

            OrderResponse response = orderService.createOrder("user123", request);

            // 33.33 * 3 = 99.99
            assertEquals(new BigDecimal("99.99"), response.getTotalAmount());
        }
    }

    @Nested
    @DisplayName("OrderModuleApi Implementation")
    class OrderModuleApiImplementation {

        @Test
        @DisplayName("Should find order by ID")
        void shouldFindOrderById() {
            Order order = Order.builder()
                    .id("order123")
                    .userId("user123")
                    .totalAmount(new BigDecimal("100.00"))
                    .status(OrderStatus.CREATED)
                    .build();

            when(orderRepository.findById("order123")).thenReturn(Optional.of(order));

            Optional<OrderModuleApi.OrderDto> result = orderService.findById("order123");

            assertTrue(result.isPresent());
            assertEquals("order123", result.get().id());
            assertEquals("CREATED", result.get().status());
        }

        @Test
        @DisplayName("Should return empty when order not found")
        void shouldReturnEmptyWhenOrderNotFound() {
            when(orderRepository.findById("nonexistent")).thenReturn(Optional.empty());

            Optional<OrderModuleApi.OrderDto> result = orderService.findById("nonexistent");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Order Confirmation")
    class OrderConfirmation {

        @Test
        @DisplayName("Should confirm CREATED order")
        void shouldConfirmCreatedOrder() {
            Order order = Order.builder()
                    .id("order123")
                    .userId("user123")
                    .status(OrderStatus.CREATED)
                    .build();

            when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            orderService.confirmOrder("order123");

            assertEquals(OrderStatus.PREPARING, order.getStatus());
            assertNotNull(order.getConfirmedAt());
        }

        @Test
        @DisplayName("Should publish OrderConfirmedEvent on confirmation")
        void shouldPublishOrderConfirmedEventOnConfirmation() {
            Order order = Order.builder()
                    .id("order123")
                    .userId("user123")
                    .status(OrderStatus.CREATED)
                    .build();

            when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            orderService.confirmOrder("order123");

            ArgumentCaptor<OrderConfirmedEvent> eventCaptor = ArgumentCaptor.forClass(OrderConfirmedEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());

            assertEquals("order123", eventCaptor.getValue().getOrderId());
        }

        @Test
        @DisplayName("Should throw BadRequestException when confirming non-CREATED order")
        void shouldThrowExceptionWhenConfirmingNonCreatedOrder() {
            Order order = Order.builder()
                    .id("order123")
                    .status(OrderStatus.CONFIRMED)
                    .build();

            when(orderRepository.findById("order123")).thenReturn(Optional.of(order));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> orderService.confirmOrder("order123"));

            assertEquals("Only placed orders can be confirmed", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent order")
        void shouldThrowExceptionForNonExistentOrder() {
            when(orderRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> orderService.confirmOrder("nonexistent"));
        }
    }

    @Nested
    @DisplayName("Order Cancellation")
    class OrderCancellation {

        @Test
        @DisplayName("Should cancel CREATED order")
        void shouldCancelCreatedOrder() {
            Order order = Order.builder()
                    .id("order123")
                    .userId("user123")
                    .items(List.of(
                            OrderItem.builder().productId("product1").quantity(2).build()
                    ))
                    .status(OrderStatus.CREATED)
                    .build();

            when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            orderService.cancelOrder("order123", "User requested");

            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            assertEquals("User requested", order.getCancellationReason());
            assertNotNull(order.getCancelledAt());
        }

        @Test
        @DisplayName("Should restore stock when cancelling order")
        void shouldRestoreStockWhenCancellingOrder() {
            Order order = Order.builder()
                    .id("order123")
                    .userId("user123")
                    .items(List.of(
                            OrderItem.builder().productId("product1").quantity(2).build(),
                            OrderItem.builder().productId("product2").quantity(3).build()
                    ))
                    .status(OrderStatus.CREATED)
                    .build();

            when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            orderService.cancelOrder("order123", "User requested");

            verify(productModuleApi).restoreStock("product1", 2);
            verify(productModuleApi).restoreStock("product2", 3);
        }

        @Test
        @DisplayName("Should throw BadRequestException when cancelling CONFIRMED order")
        void shouldThrowExceptionWhenCancellingConfirmedOrder() {
            Order order = Order.builder()
                    .id("order123")
                    .status(OrderStatus.CONFIRMED)
                    .build();

            when(orderRepository.findById("order123")).thenReturn(Optional.of(order));

            BadRequestException exception = assertThrows(BadRequestException.class, 
                    () -> orderService.cancelOrder("order123", "Reason"));
            
            assertEquals("Order cannot be cancelled in current status", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw BadRequestException when cancelling already CANCELLED order")
        void shouldThrowExceptionWhenCancellingAlreadyCancelledOrder() {
            Order order = Order.builder()
                    .id("order123")
                    .status(OrderStatus.CANCELLED)
                    .build();

            when(orderRepository.findById("order123")).thenReturn(Optional.of(order));

            assertThrows(BadRequestException.class, 
                    () -> orderService.cancelOrder("order123", "Reason"));
        }
    }

    @Nested
    @DisplayName("User Order Queries")
    class UserOrderQueries {

        @Test
        @DisplayName("Should get order by ID for user")
        void shouldGetOrderByIdForUser() {
            Order order = Order.builder()
                    .id("order123")
                    .userId("user123")
                    .totalAmount(new BigDecimal("100.00"))
                    .status(OrderStatus.CREATED)
                    .items(List.of())
                    .build();

            when(orderRepository.findByIdAndUserId("order123", "user123")).thenReturn(Optional.of(order));

            OrderResponse response = orderService.getOrder("user123", "order123");

            assertEquals("order123", response.getId());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order not found for user")
        void shouldThrowExceptionWhenOrderNotFoundForUser() {
            when(orderRepository.findByIdAndUserId("order123", "user123")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> orderService.getOrder("user123", "order123"));
        }

        @Test
        @DisplayName("Should get user orders with pagination")
        void shouldGetUserOrdersWithPagination() {
            Order order1 = Order.builder().id("order1").userId("user123")
                    .items(List.of()).status(OrderStatus.CREATED).build();
            Order order2 = Order.builder().id("order2").userId("user123")
                    .items(List.of()).status(OrderStatus.CONFIRMED).build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> orderPage = new PageImpl<>(List.of(order1, order2), pageable, 2);

            when(orderRepository.findByUserId("user123", pageable)).thenReturn(orderPage);

            Page<OrderResponse> result = orderService.getUserOrders("user123", pageable);

            assertEquals(2, result.getContent().size());
        }
    }

    @Nested
    @DisplayName("Event Handlers")
    class EventHandlers {

        @Test
        @DisplayName("Should confirm order on PaymentSuccessEvent")
        void shouldConfirmOrderOnPaymentSuccess() {
            Order order = Order.builder()
                    .id("order123")
                    .userId("user123")
                    .status(OrderStatus.PLACED)
                    .build();

            PaymentSuccessEvent event = new PaymentSuccessEvent(
                    "payment123", "order123", "user123", new BigDecimal("100.00"), "TXN123"
            );

            when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            orderService.handlePaymentSuccess(event);

            assertEquals(OrderStatus.PREPARING, order.getStatus());
        }

        @Test
        @DisplayName("Should cancel order on PaymentFailedEvent")
        void shouldCancelOrderOnPaymentFailed() {
            Order order = Order.builder()
                    .id("order123")
                    .userId("user123")
                    .status(OrderStatus.CREATED)
                    .items(List.of(
                            OrderItem.builder().productId("product1").quantity(2).build()
                    ))
                    .build();

            PaymentFailedEvent event = new PaymentFailedEvent(
                    "payment123", "order123", "user123", new BigDecimal("100.00"), "Declined"
            );

            when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            orderService.handlePaymentFailed(event);

            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            assertTrue(order.getCancellationReason().contains("Payment failed"));
        }
    }
}
