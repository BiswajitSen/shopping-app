package com.shopapp.vendor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vendors")
public class Vendor {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String businessName;

    private String description;

    private String contactEmail;

    private String contactPhone;

    @Builder.Default
    private VendorStatus status = VendorStatus.PENDING;

    private String rejectionReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime approvedAt;

    public boolean isApproved() {
        return status == VendorStatus.APPROVED;
    }

    public boolean isPending() {
        return status == VendorStatus.PENDING;
    }

    public boolean isRejected() {
        return status == VendorStatus.REJECTED;
    }
}
