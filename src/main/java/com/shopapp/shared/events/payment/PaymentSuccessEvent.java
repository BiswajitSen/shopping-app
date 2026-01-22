package com.shopapp.shared.events.payment;

import com.shopapp.shared.events.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentSuccessEvent extends DomainEvent {
    
    private final String paymentId;
    private final String orderId;
    private final String userId;
    private final BigDecimal amount;
    private final String transactionId;
    
    public PaymentSuccessEvent(String paymentId, String orderId, String userId, 
                                BigDecimal amount, String transactionId) {
        super();
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.transactionId = transactionId;
    }
    
    @Override
    public String getEventType() {
        return "PAYMENT_SUCCESS";
    }
}
