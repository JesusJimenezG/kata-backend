package dev.jesusjimenezg.kata.controller;

import dev.jesusjimenezg.kata.dto.ResourceRequest;
import dev.jesusjimenezg.kata.dto.ResourceResponse;
import dev.jesusjimenezg.kata.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Resources", description = "Shared resource management")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    @Operation(summary = "List resources", description = "Lists resources the user is allowed to see. Can filter by active status or resource type.")
    @ApiResponse(responseCode = "200", description = "Resources retrieved")
    public ResponseEntity<List<ResourceResponse>> findAll(
            @Parameter(description = "Filter by active status (true/false)") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Filter by resource type ID") @RequestParam(required = false) Integer typeId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (Boolean.TRUE.equals(active)) {
            return ResponseEntity.ok(resourceService.findActive(userDetails));
        }
        if (typeId != null) {
            return ResponseEntity.ok(resourceService.findByType(typeId, userDetails));
        }
        return ResponseEntity.ok(resourceService.findAll(userDetails));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resource by ID", description = "Returns details of a specific resource if the user has permission.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource found", content = @Content(schema = @Schema(implementation = ResourceResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (insufficient permissions)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<ResourceResponse> findById(
            @Parameter(description = "UUID of the resource") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(resourceService.findById(id, userDetails));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a resource", description = "Creates a new resource. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = ResourceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)", content = @Content),
            @ApiResponse(responseCode = "409", description = "Duplicate name", content = @Content)
    })
    public ResponseEntity<ResourceResponse> create(@RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a resource", description = "Updates an existing resource. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = ResourceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<ResourceResponse> update(
            @Parameter(description = "UUID of the resource to update") @PathVariable UUID id,
            @RequestBody ResourceRequest request) {
        return ResponseEntity.ok(resourceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a resource", description = "Deactivates the resource (soft delete). Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID of the resource to delete") @PathVariable UUID id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
