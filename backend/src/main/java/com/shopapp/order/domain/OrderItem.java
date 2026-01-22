package com.shopapp.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private String productId;
    private String productName;
    private String productImage;
    private String vendorId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public static OrderItem create(String productId, String productName, String productImage,
                                   String vendorId, int quantity, BigDecimal unitPrice) {
        return OrderItem.builder()
                .productId(productId)
                .productName(productName)
                .productImage(productImage)
                .vendorId(vendorId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .subtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .build();
    }
}
