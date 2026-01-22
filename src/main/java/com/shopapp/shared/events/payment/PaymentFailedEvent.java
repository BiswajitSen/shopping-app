package com.shopapp.shared.events.payment;

import com.shopapp.shared.events.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentFailedEvent extends DomainEvent {
    
    private final String paymentId;
    private final String orderId;
    private final String userId;
    private final BigDecimal amount;
    private final String failureReason;
    
    public PaymentFailedEvent(String paymentId, String orderId, String userId, 
                               BigDecimal amount, String failureReason) {
        super();
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.failureReason = failureReason;
    }
    
    @Override
    public String getEventType() {
        return "PAYMENT_FAILED";
    }
}
