package com.shopapp.vendor.dto;

import com.shopapp.vendor.domain.VendorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponse {

    private String id;
    private String userId;
    private String businessName;
    private String description;
    private String contactEmail;
    private String contactPhone;
    private VendorStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}
