package dev.jesusjimenezg.kata.repository;

import dev.jesusjimenezg.kata.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    boolean existsByName(String name);

    List<Resource> findByActiveTrue();

    List<Resource> findByResourceTypeId(Integer resourceTypeId);

    /** All resources filtered by allowed resource type IDs. */
    List<Resource> findByResourceTypeIdIn(Collection<Integer> resourceTypeIds);

    /** Active resources filtered by allowed resource type IDs. */
    List<Resource> findByActiveTrueAndResourceTypeIdIn(Collection<Integer> resourceTypeIds);

    /** Resources of a specific type, further constrained by allowed types. */
    List<Resource> findByResourceTypeIdAndResourceTypeIdIn(Integer resourceTypeId,
            Collection<Integer> allowedTypeIds);
}
