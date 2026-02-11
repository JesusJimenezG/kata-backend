package dev.jesusjimenezg.kata.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
