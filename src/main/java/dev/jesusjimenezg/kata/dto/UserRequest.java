package dev.jesusjimenezg.kata.dto;

import java.util.Set;

public record UserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        Set<String> roles) {
}
