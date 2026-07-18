package com.uepb.DesafioKnex.dto.request;

import com.uepb.DesafioKnex.model.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UserRequest(

        @NotNull
        String name,
        @NotNull
        String email,
        @NotNull
        String password,
        @NotNull
        Role role
) {
}
