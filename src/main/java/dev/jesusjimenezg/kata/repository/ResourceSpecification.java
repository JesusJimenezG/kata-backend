package dev.jesusjimenezg.kata.repository;

import dev.jesusjimenezg.kata.model.Resource;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

/**
 * Dynamic JPA Specifications for filtering resources.
 */
public final class ResourceSpecification {

    private ResourceSpecification() {
    }

    /**
     * Restrict to the given set of allowed resource-type IDs (permission filter).
     */
    public static Specification<Resource> hasTypeIdIn(Collection<Integer> typeIds) {
        return (root, query, cb) -> root.get("resourceType").get("id").in(typeIds);
    }

    /** Restrict to a specific resource-type ID. */
    public static Specification<Resource> hasTypeId(Integer typeId) {
        return (root, query, cb) -> cb.equal(root.get("resourceType").get("id"), typeId);
    }

    /** Only active resources. */
    public static Specification<Resource> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    /**
     * Case-insensitive LIKE search across name, description, and location.
     */
    public static Specification<Resource> searchText(String text) {
        return (root, query, cb) -> {
            String pattern = "%" + text.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("location")), pattern));
        };
    }
}
