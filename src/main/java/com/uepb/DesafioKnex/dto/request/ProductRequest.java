package com.uepb.DesafioKnex.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(

        @NotNull
        @Size(max = 100)
        String name,
        @NotNull
        @Size(max = 255)
        String description,
        @NotNull
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal price,
        @NotNull
        Integer stockQuantity
) {
}
