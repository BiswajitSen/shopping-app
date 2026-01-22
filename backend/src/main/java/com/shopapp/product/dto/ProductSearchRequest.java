package com.shopapp.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {

    private String keyword;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String vendorId;
}
