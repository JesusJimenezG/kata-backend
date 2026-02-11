package dev.jesusjimenezg.kata.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResourceResponse(
        UUID id,
        String name,
        String description,
        ResourceTypeResponse resourceType,
        String location,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
