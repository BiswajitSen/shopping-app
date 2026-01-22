package com.shopapp.product.dto;

import com.shopapp.product.domain.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String id;
    private String name;
    private String category;
    private BigDecimal price;
    private int stock;
    private String description;
    private List<String> images;
    private String vendorId;
    private String vendorName;
    private ProductStatus status;
    private String rejectionReason;
    private boolean visible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
