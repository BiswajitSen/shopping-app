package com.shopapp.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResponse {
    
    private int totalRows;
    private int successCount;
    private int failureCount;
    
    @Builder.Default
    private List<ProductResponse> successfulProducts = new ArrayList<>();
    
    @Builder.Default
    private List<RowError> errors = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {
        private int rowNumber;
        private String productName;
        private String errorMessage;
    }
}
