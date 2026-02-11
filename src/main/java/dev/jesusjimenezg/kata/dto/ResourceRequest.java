package dev.jesusjimenezg.kata.dto;

public record ResourceRequest(
        String name,
        String description,
        Integer resourceTypeId,
        String location) {
}
