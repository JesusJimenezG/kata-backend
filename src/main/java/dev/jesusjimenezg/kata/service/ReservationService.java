package dev.jesusjimenezg.kata.service;

import dev.jesusjimenezg.kata.dto.AvailabilitySlot;
import dev.jesusjimenezg.kata.dto.ReservationRequest;
import dev.jesusjimenezg.kata.dto.ReservationResponse;
import dev.jesusjimenezg.kata.model.AppUser;
import dev.jesusjimenezg.kata.model.Reservation;
import dev.jesusjimenezg.kata.model.Resource;
import dev.jesusjimenezg.kata.repository.AppUserRepository;
import dev.jesusjimenezg.kata.repository.ReservationRepository;
import dev.jesusjimenezg.kata.repository.ResourceRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final AppUserRepository appUserRepository;
    private final ResourcePermissionService permissionService;

    public ReservationService(ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            AppUserRepository appUserRepository,
            ResourcePermissionService permissionService) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.appUserRepository = appUserRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request, UserDetails userDetails) {
        if (request.startTime() == null || request.endTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Resource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + request.resourceId()));

        if (!resource.isActive()) {
            throw new IllegalArgumentException("Resource is not active: " + request.resourceId());
        }

        // Role-based permission check for the resource type
        permissionService.checkAccess(userDetails, resource.getResourceType().getId());

        // Check for overlapping active reservations
        if (reservationRepository.existsOverlapping(request.resourceId(), request.startTime(), request.endTime())) {
            throw new IllegalStateException(
                    "Time slot overlaps with an existing active reservation for this resource");
        }

        AppUser user = appUserRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        Reservation reservation = new Reservation();
        reservation.setResource(resource);
        reservation.setUser(user);
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());
        reservation.setNotes(request.notes());

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(UUID id, UserDetails userDetails) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + id));
        permissionService.checkAccess(userDetails, reservation.getResource().getResourceType().getId());
        return toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findActiveByUser(UserDetails userDetails) {
        AppUser user = appUserRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        Set<Integer> allowed = permissionService.getAllowedResourceTypeIds(userDetails);
        return reservationRepository.findByUserIdAndStatusAndResourceTypeIdIn(user.getId(), "ACTIVE", allowed)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllActive(UserDetails userDetails) {
        Set<Integer> allowed = permissionService.getAllowedResourceTypeIds(userDetails);
        return reservationRepository.findByStatusAndResourceTypeIdIn("ACTIVE", allowed).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findHistoryByResource(UUID resourceId, UserDetails userDetails) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));
        permissionService.checkAccess(userDetails, resource.getResourceType().getId());
        return reservationRepository.findByResourceIdOrderByStartTimeDesc(resourceId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findHistoryByUser(UserDetails userDetails) {
        AppUser user = appUserRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        Set<Integer> allowed = permissionService.getAllowedResourceTypeIds(userDetails);
        return reservationRepository.findByUserIdAndResourceTypeIdInOrderByStartTimeDesc(user.getId(), allowed)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReservationResponse cancel(UUID reservationId, UserDetails userDetails) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        if (!"ACTIVE".equals(reservation.getStatus())) {
            throw new IllegalStateException("Only active reservations can be cancelled");
        }

        AppUser currentUser = appUserRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        // Only the creator or an ADMIN can cancel
        boolean isOwner = reservation.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Only the reservation creator or an admin can cancel this reservation");
        }

        reservation.setStatus("CANCELLED");
        reservation.setCancelledBy(currentUser);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());

        return toResponse(reservationRepository.save(reservation));
    }

    /**
     * Returns availability slots for a resource within a time window.
     * Splits the window into available/reserved intervals.
     */
    @Transactional(readOnly = true)
    public List<AvailabilitySlot> getAvailability(UUID resourceId, LocalDateTime windowStart,
            LocalDateTime windowEnd, UserDetails userDetails) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));
        permissionService.checkAccess(userDetails, resource.getResourceType().getId());

        if (!windowEnd.isAfter(windowStart)) {
            throw new IllegalArgumentException("Window end must be after window start");
        }

        List<Reservation> active = reservationRepository.findActiveInWindow(resourceId, windowStart, windowEnd);
        List<AvailabilitySlot> slots = new ArrayList<>();

        LocalDateTime cursor = windowStart;

        for (Reservation r : active) {
            LocalDateTime reservationStart = r.getStartTime().isBefore(windowStart) ? windowStart : r.getStartTime();
            LocalDateTime reservationEnd = r.getEndTime().isAfter(windowEnd) ? windowEnd : r.getEndTime();

            // Free gap before this reservation
            if (cursor.isBefore(reservationStart)) {
                slots.add(new AvailabilitySlot(cursor, reservationStart, true));
            }

            // Reserved slot
            slots.add(new AvailabilitySlot(reservationStart, reservationEnd, false));

            cursor = reservationEnd;
        }

        // Trailing free gap
        if (cursor.isBefore(windowEnd)) {
            slots.add(new AvailabilitySlot(cursor, windowEnd, true));
        }

        return slots;
    }

    private ReservationResponse toResponse(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getResource().getId(),
                r.getResource().getName(),
                r.getUser().getId(),
                r.getUser().getEmail(),
                r.getStartTime(),
                r.getEndTime(),
                r.getStatus(),
                r.getNotes(),
                r.getCancelledBy() != null ? r.getCancelledBy().getId() : null,
                r.getCancelledAt(),
                r.getCreatedAt(),
                r.getUpdatedAt());
    }
}
