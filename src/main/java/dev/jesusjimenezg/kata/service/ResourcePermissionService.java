package dev.jesusjimenezg.kata.service;

import dev.jesusjimenezg.kata.repository.ResourceTypeRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Central service for role-based resource type permissions.
 * Queries the {@code role_resource_type_permission} table to determine
 * which resource types are accessible to the authenticated user.
 */
@Service
public class ResourcePermissionService {

    private final ResourceTypeRepository resourceTypeRepository;

    public ResourcePermissionService(ResourceTypeRepository resourceTypeRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
    }

    /**
     * Returns the set of resource type IDs that the authenticated user can access,
     * computed as the union of permissions from all assigned roles.
     */
    public Set<Integer> getAllowedResourceTypeIds(UserDetails userDetails) {
        Collection<String> roleNames = extractRoleNames(userDetails);
        return new HashSet<>(resourceTypeRepository.findAllowedResourceTypeIdsByRoleNames(roleNames));
    }

    /**
     * Returns {@code true} if the user can access the given resource type.
     */
    public boolean canAccessResourceType(UserDetails userDetails, Integer resourceTypeId) {
        return getAllowedResourceTypeIds(userDetails).contains(resourceTypeId);
    }

    /**
     * Throws {@link AccessDeniedException} if the user cannot access the given
     * resource type.
     */
    public void checkAccess(UserDetails userDetails, Integer resourceTypeId) {
        if (!canAccessResourceType(userDetails, resourceTypeId)) {
            throw new AccessDeniedException(
                    "You do not have permission to access resources of this type");
        }
    }

    private Set<String> extractRoleNames(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .collect(Collectors.toSet());
    }
}
