package com.uepb.DesafioKnex.dto.request;

public record AuthRequest(
        String email,
        String password
) {
}
