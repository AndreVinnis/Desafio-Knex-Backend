package com.uepb.DesafioKnex.dto.response;

import java.math.BigDecimal;

public record ProductResponse(

        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity
) {
}
