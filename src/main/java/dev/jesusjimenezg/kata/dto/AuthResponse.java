package dev.jesusjimenezg.kata.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email) {
}
