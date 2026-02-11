package dev.jesusjimenezg.kata.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID resourceId,
        String resourceName,
        UUID userId,
        String userEmail,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        String notes,
        UUID cancelledById,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
