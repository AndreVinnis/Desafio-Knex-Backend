package com.uepb.DesafioKnex.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        BigDecimal subtotal,
        List<CartItemResponse> items
) {
}
