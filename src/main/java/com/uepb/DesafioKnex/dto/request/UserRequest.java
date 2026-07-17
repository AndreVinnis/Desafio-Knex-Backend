package com.uepb.DesafioKnex.dto.request;

import com.uepb.DesafioKnex.model.enums.Role;

public record UserRequest(
        String name,
        String email,
        String password,
        Role role
) {
}
