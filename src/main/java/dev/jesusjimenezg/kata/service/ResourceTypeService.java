package dev.jesusjimenezg.kata.service;

import dev.jesusjimenezg.kata.dto.ResourceTypeRequest;
import dev.jesusjimenezg.kata.dto.ResourceTypeResponse;
import dev.jesusjimenezg.kata.model.ResourceType;
import dev.jesusjimenezg.kata.repository.ResourceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceTypeService {

    private final ResourceTypeRepository resourceTypeRepository;

    public ResourceTypeService(ResourceTypeRepository resourceTypeRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceTypeResponse> findAll() {
        return resourceTypeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceTypeResponse findById(Integer id) {
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
