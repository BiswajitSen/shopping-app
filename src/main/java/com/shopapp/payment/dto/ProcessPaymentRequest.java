package com.shopapp.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    // In a real scenario, this would include payment details like card info
    // For MVP, we'll simulate payment processing
    private boolean simulateSuccess;
}
