package dev.jesusjimenezg.kata.controller;

import dev.jesusjimenezg.kata.dto.AvailabilitySlot;
import dev.jesusjimenezg.kata.dto.ReservationRequest;
import dev.jesusjimenezg.kata.dto.ReservationResponse;
import dev.jesusjimenezg.kata.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Reservations", description = "Resource reservation management")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Create a reservation", description = "Creates a reservation for a resource. Prevents overlapping active reservations.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservation created", content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "409", description = "Time slot overlaps", content = @Content)
    })
    public ResponseEntity<ReservationResponse> create(
            @RequestBody ReservationRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.create(request, userDetails));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", description = "Returns details of a specific reservation if the user has permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found", content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (insufficient permissions)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<ReservationResponse> findById(
            @Parameter(description = "UUID of the reservation") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.findById(id, userDetails));
    }

    @GetMapping("/active")
    @Operation(summary = "List all active reservations", description = "Filtered by the caller's role-based resource type permissions.")
    @ApiResponse(responseCode = "200", description = "Active reservations retrieved")
    public ResponseEntity<List<ReservationResponse>> findAllActive(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.findAllActive(userDetails));
    }

    @GetMapping("/my")
    @Operation(summary = "List active reservations for current user")
    @ApiResponse(responseCode = "200", description = "User's active reservations retrieved")
    public ResponseEntity<List<ReservationResponse>> findMyActive(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.findActiveByUser(userDetails));
    }

    @GetMapping("/my/history")
    @Operation(summary = "Reservation history for current user")
    @ApiResponse(responseCode = "200", description = "User's reservation history retrieved")
    public ResponseEntity<List<ReservationResponse>> findMyHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.findHistoryByUser(userDetails));
    }

    @GetMapping("/resource/{resourceId}/history")
    @Operation(summary = "Reservation history for a resource")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource reservation history retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden (insufficient permissions)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content)
    })
    public ResponseEntity<List<ReservationResponse>> findResourceHistory(
            @Parameter(description = "UUID of the resource") @PathVariable UUID resourceId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.findHistoryByResource(resourceId, userDetails));
    }

    @GetMapping("/resource/{resourceId}/availability")
    @Operation(summary = "Get availability slots for a resource", description = "Returns available and reserved time slots within a window")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden (insufficient permissions)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content)
    })
    public ResponseEntity<List<AvailabilitySlot>> getAvailability(
            @Parameter(description = "UUID of the resource") @PathVariable UUID resourceId,
            @Parameter(description = "Start of window (ISO date-time)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "End of window (ISO date-time)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.getAvailability(resourceId, start, end, userDetails));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation", description = "Only the creator or an admin can cancel.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation cancelled", content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (Not creator or Admin)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<ReservationResponse> cancel(
            @Parameter(description = "UUID of the reservation to cancel") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.cancel(id, userDetails));
    }
}
