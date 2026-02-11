package dev.jesusjimenezg.kata.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationRequest(
        UUID resourceId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String notes) {
}
