package dev.jesusjimenezg.kata.dto;

import java.util.Set;

public record UserUpdateRequest(
        String firstName,
        String lastName,
        Set<String> roles,
        Boolean enabled) {
}
