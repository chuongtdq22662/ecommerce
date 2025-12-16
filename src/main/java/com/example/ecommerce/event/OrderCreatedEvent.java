package com.example.ecommerce.event;

import java.io.Serializable;
import java.math.BigDecimal;

public record OrderCreatedEvent(
        Long orderId,
        String username,
        String email,
        BigDecimal totalAmount
) implements Serializable {}