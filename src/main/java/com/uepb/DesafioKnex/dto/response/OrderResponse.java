package com.uepb.DesafioKnex.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        BigDecimal totalAmount,
        List<OrderItemResponse> items
) {
}
