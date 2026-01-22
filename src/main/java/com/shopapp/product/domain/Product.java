package com.shopapp.product.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String name;

    @Indexed
    private String category;

    private BigDecimal price;

    private int stock;

    private String description;

    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Indexed
    private String vendorId;

    @Indexed
    @Builder.Default
    private ProductStatus status = ProductStatus.PENDING;

    private String rejectionReason;

    @Builder.Default
    private boolean visible = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public boolean isApproved() {
        return status == ProductStatus.APPROVED;
    }

    public boolean isPending() {
        return status == ProductStatus.PENDING;
    }

    public boolean isRejected() {
        return status == ProductStatus.REJECTED;
    }

    public boolean isAvailableForPurchase() {
        return isApproved() && visible && stock > 0;
    }

    public boolean hasStock(int quantity) {
        return stock >= quantity;
    }

    public void decrementStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.stock -= quantity;
    }

    public void incrementStock(int quantity) {
        this.stock += quantity;
    }
}
