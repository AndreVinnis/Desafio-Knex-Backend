package com.uepb.DesafioKnex.dto.response;

import com.uepb.DesafioKnex.model.enums.Role;

import java.time.Instant;

public record UserResponse(

        String name,
        String email,
        Role role,
        Instant createdAt
) {
}
