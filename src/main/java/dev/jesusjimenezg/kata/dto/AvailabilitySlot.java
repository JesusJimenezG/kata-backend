package dev.jesusjimenezg.kata.dto;

import java.time.LocalDateTime;

public record AvailabilitySlot(
        LocalDateTime start,
        LocalDateTime end,
        boolean available) {
}
