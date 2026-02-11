package dev.jesusjimenezg.kata.service;

import dev.jesusjimenezg.kata.dto.ResourceRequest;
import dev.jesusjimenezg.kata.dto.ResourceResponse;
import dev.jesusjimenezg.kata.dto.ResourceTypeResponse;
import dev.jesusjimenezg.kata.model.Resource;
import dev.jesusjimenezg.kata.model.ResourceType;
import dev.jesusjimenezg.kata.repository.ResourceRepository;
import dev.jesusjimenezg.kata.repository.ResourceSpecification;
import dev.jesusjimenezg.kata.repository.ResourceTypeRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final ResourcePermissionService permissionService;

    public ResourceService(ResourceRepository resourceRepository,
            ResourceTypeRepository resourceTypeRepository,
            ResourcePermissionService permissionService) {
        this.resourceRepository = resourceRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.permissionService = permissionService;
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> findAll(UserDetails userDetails) {
        Set<Integer> allowed = permissionService.getAllowedResourceTypeIds(userDetails);
        return resourceRepository.findByResourceTypeIdIn(allowed).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> findActive(UserDetails userDetails) {
        Set<Integer> allowed = permissionService.getAllowedResourceTypeIds(userDetails);
        return resourceRepository.findByActiveTrueAndResourceTypeIdIn(allowed).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> findByType(Integer resourceTypeId, UserDetails userDetails) {
        permissionService.checkAccess(userDetails, resourceTypeId);
        return resourceRepository.findByResourceTypeId(resourceTypeId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Unified search: combines optional text search, active filter, and type
     * filter.
     * Always constrained by the user's allowed resource types.
     */
    @Transactional(readOnly = true)
    public List<ResourceResponse> search(String query, Boolean active, Integer typeId, UserDetails userDetails) {
        Set<Integer> allowed = permissionService.getAllowedResourceTypeIds(userDetails);

        Specification<Resource> spec = Specification.where(ResourceSpecification.hasTypeIdIn(allowed));

        if (active != null && active) {
            spec = spec.and(ResourceSpecification.isActive());
        }
        if (typeId != null) {
            spec = spec.and(ResourceSpecification.hasTypeId(typeId));
        }
        if (query != null && !query.isBlank()) {
            spec = spec.and(ResourceSpecification.searchText(query.strip()));
        }

        return resourceRepository.findAll(spec).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceResponse findById(UUID id, UserDetails userDetails) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + id));
        permissionService.checkAccess(userDetails, resource.getResourceType().getId());
        return toResponse(resource);
    }

    @Transactional
    public ResourceResponse create(ResourceRequest request) {
        if (resourceRepository.existsByName(request.name())) {
            throw new IllegalStateException("Resource name already exists: " + request.name());
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
            throw new IllegalStateException("Resource name already exists: " + request.name());
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
