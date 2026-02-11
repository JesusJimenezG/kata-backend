package dev.jesusjimenezg.kata.repository;

import dev.jesusjimenezg.kata.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

  /** Active reservations for a specific user. */
  List<Reservation> findByUserIdAndStatus(UUID userId, String status);

  /** All active reservations (global view). */
  List<Reservation> findByStatus(String status);

  /** Active reservations for a specific resource. */
  List<Reservation> findByResourceIdAndStatus(UUID resourceId, String status);

  /**
   * Detect overlapping ACTIVE reservations for the same resource.
   * Two intervals [s1, e1) and [s2, e2) overlap when s1 < e2 AND s2 < e1.
   */
  @Query("""
      SELECT COUNT(r) > 0 FROM Reservation r
      WHERE r.resource.id = :resourceId
        AND r.status = 'ACTIVE'
        AND r.startTime < :endTime
        AND r.endTime > :startTime
      """)
  boolean existsOverlapping(UUID resourceId, LocalDateTime startTime, LocalDateTime endTime);

  /**
   * Reservation history for a resource (all statuses, ordered by most recent
   * first).
   */
  List<Reservation> findByResourceIdOrderByStartTimeDesc(UUID resourceId);

  /**
   * Reservation history for a user (all statuses, ordered by most recent first).
   */
  List<Reservation> findByUserIdOrderByStartTimeDesc(UUID userId);

  /**
   * Active reservations for a resource within a time window (availability view).
   */
  @Query("""
      SELECT r FROM Reservation r
      WHERE r.resource.id = :resourceId
        AND r.status = 'ACTIVE'
        AND r.startTime < :windowEnd
        AND r.endTime > :windowStart
      ORDER BY r.startTime
      """)
  List<Reservation> findActiveInWindow(UUID resourceId, LocalDateTime windowStart, LocalDateTime windowEnd);

  // ── Permission-filtered queries ──────────────────────────────────────

  /** All reservations with a given status, filtered by allowed resource types. */
  @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.resource.resourceType.id IN :typeIds")
  List<Reservation> findByStatusAndResourceTypeIdIn(@Param("status") String status,
      @Param("typeIds") Collection<Integer> typeIds);

  /**
   * User's reservations with a given status, filtered by allowed resource types.
   */
  @Query("""
      SELECT r FROM Reservation r
      WHERE r.user.id = :userId
        AND r.status = :status
        AND r.resource.resourceType.id IN :typeIds
      """)
  List<Reservation> findByUserIdAndStatusAndResourceTypeIdIn(@Param("userId") UUID userId,
      @Param("status") String status, @Param("typeIds") Collection<Integer> typeIds);

  /** Resource reservation history, filtered by allowed resource types. */
  @Query("""
      SELECT r FROM Reservation r
      WHERE r.resource.id = :resourceId
        AND r.resource.resourceType.id IN :typeIds
      ORDER BY r.startTime DESC
      """)
  List<Reservation> findByResourceIdAndResourceTypeIdInOrderByStartTimeDesc(
      @Param("resourceId") UUID resourceId, @Param("typeIds") Collection<Integer> typeIds);

  /** User's reservation history, filtered by allowed resource types. */
  @Query("""
      SELECT r FROM Reservation r
      WHERE r.user.id = :userId
        AND r.resource.resourceType.id IN :typeIds
      ORDER BY r.startTime DESC
      """)
  List<Reservation> findByUserIdAndResourceTypeIdInOrderByStartTimeDesc(@Param("userId") UUID userId,
      @Param("typeIds") Collection<Integer> typeIds);
}
