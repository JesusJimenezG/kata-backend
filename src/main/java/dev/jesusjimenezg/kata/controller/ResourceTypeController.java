package dev.jesusjimenezg.kata.controller;

import dev.jesusjimenezg.kata.dto.ResourceTypeRequest;
import dev.jesusjimenezg.kata.dto.ResourceTypeResponse;
import dev.jesusjimenezg.kata.service.ResourceTypeService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resource-types")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Resource Types", description = "Resource type catalogue management")
public class ResourceTypeController {

    private final ResourceTypeService resourceTypeService;

    public ResourceTypeController(ResourceTypeService resourceTypeService) {
        this.resourceTypeService = resourceTypeService;
    }

    @GetMapping
    @Operation(summary = "List all resource types")
    @ApiResponse(responseCode = "200", description = "Resource types retrieved")
    public ResponseEntity<List<ResourceTypeResponse>> findAll() {
        return ResponseEntity.ok(resourceTypeService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resource type by ID", description = "Returns details of a specific resource type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource type found", content = @Content(schema = @Schema(implementation = ResourceTypeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<ResourceTypeResponse> findById(
            @Parameter(description = "ID of the resource type", example = "1") @PathVariable Integer id) {
        return ResponseEntity.ok(resourceTypeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a resource type", description = "Creates a new resource type. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = ResourceTypeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)", content = @Content),
            @ApiResponse(responseCode = "409", description = "Duplicate name", content = @Content)
    })
    public ResponseEntity<ResourceTypeResponse> create(@RequestBody ResourceTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceTypeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a resource type", description = "Updates an existing resource type. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = ResourceTypeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<ResourceTypeResponse> update(
            @Parameter(description = "ID of the resource type to update", example = "1") @PathVariable Integer id,
            @RequestBody ResourceTypeRequest request) {
        return ResponseEntity.ok(resourceTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a resource type", description = "Deletes a resource type. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the resource type to delete", example = "1") @PathVariable Integer id) {
        resourceTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
