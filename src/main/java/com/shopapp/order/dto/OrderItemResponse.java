package com.shopapp.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private String productId;
    private String productName;
    private String vendorId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
