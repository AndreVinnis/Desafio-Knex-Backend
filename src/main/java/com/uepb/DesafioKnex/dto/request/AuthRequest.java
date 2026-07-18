package com.uepb.DesafioKnex.dto.request;

import jakarta.validation.constraints.NotNull;

public record AuthRequest(

        @NotNull
        String email,
        @NotNull
        String password
) {
}
