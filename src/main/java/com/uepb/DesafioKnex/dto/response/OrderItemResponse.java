package com.uepb.DesafioKnex.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        String productName,
        Integer quantity,
        BigDecimal uniquePrice,
        BigDecimal subtotal
) {
}
