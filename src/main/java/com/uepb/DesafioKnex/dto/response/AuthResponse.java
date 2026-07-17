package com.uepb.DesafioKnex.dto.response;

public record AuthResponse(
        String token,
        String tokenType,
        Long  expiresIn
) {
    public AuthResponse(String token, long expiresIn) {
        this(token, "Bearer", expiresIn);
    }
}
