package com.uepb.DesafioKnex.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        String productName,
        Integer quantity,
        BigDecimal uniquePrice,
        BigDecimal subtotal
) {
}
