package dev.jesusjimenezg.kata.repository;

import dev.jesusjimenezg.kata.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourceTypeRepository extends JpaRepository<ResourceType, Integer> {

    Optional<ResourceType> findByName(String name);
}
