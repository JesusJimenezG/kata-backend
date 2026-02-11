package dev.jesusjimenezg.kata.repository;

import dev.jesusjimenezg.kata.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    boolean existsByName(String name);

    List<Resource> findByActiveTrue();

    List<Resource> findByResourceTypeId(Integer resourceTypeId);
}
