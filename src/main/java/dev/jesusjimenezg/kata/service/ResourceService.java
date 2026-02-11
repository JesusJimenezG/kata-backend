package dev.jesusjimenezg.kata.service;

import dev.jesusjimenezg.kata.dto.ResourceRequest;
import dev.jesusjimenezg.kata.dto.ResourceResponse;
import dev.jesusjimenezg.kata.dto.ResourceTypeResponse;
import dev.jesusjimenezg.kata.model.Resource;
import dev.jesusjimenezg.kata.model.ResourceType;
import dev.jesusjimenezg.kata.repository.ResourceRepository;
import dev.jesusjimenezg.kata.repository.ResourceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceTypeRepository resourceTypeRepository;

    public ResourceService(ResourceRepository resourceRepository,
            ResourceTypeRepository resourceTypeRepository) {
        this.resourceRepository = resourceRepository;
        this.resourceTypeRepository = resourceTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> findAll() {
        return resourceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> findActive() {
        return resourceRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> findByType(Integer resourceTypeId) {
        return resourceRepository.findByResourceTypeId(resourceTypeId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceResponse findById(UUID id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + id));
        return toResponse(resource);
    }

    @Transactional
    public ResourceResponse create(ResourceRequest request) {
        if (resourceRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Resource name already exists: " + request.name());
        }

        ResourceType type = resourceTypeRepository.findById(request.resourceTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resource type not found: " + request.resourceTypeId()));

        Resource resource = new Resource();
        resource.setName(request.name());
        resource.setDescription(request.description());
        resource.setResourceType(type);
        resource.setLocation(request.location());
        return toResponse(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceResponse update(UUID id, ResourceRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + id));

        // Check name uniqueness if changing
        if (!resource.getName().equals(request.name()) && resourceRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Resource name already exists: " + request.name());
        }

        ResourceType type = resourceTypeRepository.findById(request.resourceTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resource type not found: " + request.resourceTypeId()));

        resource.setName(request.name());
        resource.setDescription(request.description());
        resource.setResourceType(type);
        resource.setLocation(request.location());
        resource.setUpdatedAt(LocalDateTime.now());
        return toResponse(resourceRepository.save(resource));
    }

    @Transactional
    public void delete(UUID id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + id));
        resource.setActive(false);
        resource.setUpdatedAt(LocalDateTime.now());
        resourceRepository.save(resource);
    }

    private ResourceResponse toResponse(Resource r) {
        ResourceType rt = r.getResourceType();
        return new ResourceResponse(
                r.getId(),
                r.getName(),
                r.getDescription(),
                new ResourceTypeResponse(rt.getId(), rt.getName(), rt.getDescription()),
                r.getLocation(),
                r.isActive(),
                r.getCreatedAt(),
                r.getUpdatedAt());
    }
}
