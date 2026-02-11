package dev.jesusjimenezg.kata.repository;

import dev.jesusjimenezg.kata.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceTypeRepository extends JpaRepository<ResourceType, Integer> {

    Optional<ResourceType> findByName(String name);

    /** Returns the resource type IDs that any of the given roles can access. */
    @Query(value = """
            SELECT DISTINCT rtp.resource_type_id
            FROM role_resource_type_permission rtp
            JOIN role r ON rtp.role_id = r.id
            WHERE r.name IN :roleNames
            """, nativeQuery = true)
    List<Integer> findAllowedResourceTypeIdsByRoleNames(@Param("roleNames") Collection<String> roleNames);

    /** Resource types filtered by a set of IDs. */
    List<ResourceType> findByIdIn(Collection<Integer> ids);
}
