package com.shopflow.user.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String email,
        String fullName,
        String role
) {
    public static AuthResponse of(String accessToken, Long userId, String email, String fullName, String role) {
        return new AuthResponse(accessToken, "Bearer", userId, email, fullName, role);
    }
}