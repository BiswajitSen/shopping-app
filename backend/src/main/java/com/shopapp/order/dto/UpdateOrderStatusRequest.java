package com.shopapp.order.dto;

import com.shopapp.order.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;
    
    // Optional note about the status update
    private String note;
    
    // Required when status is DELIVERY_SCHEDULED
    private LocalDate estimatedDeliveryDate;
}
