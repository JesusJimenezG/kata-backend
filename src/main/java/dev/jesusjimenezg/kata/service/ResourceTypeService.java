package dev.jesusjimenezg.kata.service;

import dev.jesusjimenezg.kata.dto.ResourceTypeRequest;
import dev.jesusjimenezg.kata.dto.ResourceTypeResponse;
import dev.jesusjimenezg.kata.model.ResourceType;
import dev.jesusjimenezg.kata.repository.ResourceTypeRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ResourceTypeService {

    private final ResourceTypeRepository resourceTypeRepository;
    private final ResourcePermissionService permissionService;

    public ResourceTypeService(ResourceTypeRepository resourceTypeRepository,
            ResourcePermissionService permissionService) {
        this.resourceTypeRepository = resourceTypeRepository;
        this.permissionService = permissionService;
    }

    @Transactional(readOnly = true)
    public List<ResourceTypeResponse> findAll(UserDetails userDetails) {
        Set<Integer> allowed = permissionService.getAllowedResourceTypeIds(userDetails);
        return resourceTypeRepository.findByIdIn(allowed).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceTypeResponse findById(Integer id, UserDetails userDetails) {
        permissionService.checkAccess(userDetails, id);
        ResourceType rt = resourceTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource type not found: " + id));
        return toResponse(rt);
    }

    @Transactional
    public ResourceTypeResponse create(ResourceTypeRequest request) {
        if (resourceTypeRepository.findByName(request.name()).isPresent()) {
            throw new IllegalArgumentException("Resource type name already exists: " + request.name());
        }
        ResourceType rt = new ResourceType(request.name(), request.description());
        return toResponse(resourceTypeRepository.save(rt));
    }

    @Transactional
    public ResourceTypeResponse update(Integer id, ResourceTypeRequest request) {
        ResourceType rt = resourceTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource type not found: " + id));

        resourceTypeRepository.findByName(request.name()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Resource type name already exists: " + request.name());
            }
        });

        rt.setName(request.name());
        rt.setDescription(request.description());
        return toResponse(resourceTypeRepository.save(rt));
    }

    @Transactional
    public void delete(Integer id) {
        if (!resourceTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Resource type not found: " + id);
        }
        resourceTypeRepository.deleteById(id);
    }

    private ResourceTypeResponse toResponse(ResourceType rt) {
        return new ResourceTypeResponse(rt.getId(), rt.getName(), rt.getDescription());
    }
}
