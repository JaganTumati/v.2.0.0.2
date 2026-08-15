package com.restaurant.ai.controller;

import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.dto.KnowledgeDocumentRequest;
import com.restaurant.ai.dto.KnowledgeDocumentResponse;
import com.restaurant.ai.service.KnowledgeDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService knowledgeDocumentService;
    private final RestaurantProperties restaurantProperties;

    public KnowledgeDocumentController(KnowledgeDocumentService knowledgeDocumentService, RestaurantProperties restaurantProperties) {
        this.knowledgeDocumentService = knowledgeDocumentService;
        this.restaurantProperties = restaurantProperties;
    }

    @GetMapping
    public List<KnowledgeDocumentResponse> list() {
        return knowledgeDocumentService.listAll(restaurantProperties.getDefaultId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER')")
    public KnowledgeDocumentResponse create(@Valid @RequestBody KnowledgeDocumentRequest request) {
        return knowledgeDocumentService.create(restaurantProperties.getDefaultId(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER')")
    public KnowledgeDocumentResponse update(@PathVariable UUID id, @Valid @RequestBody KnowledgeDocumentRequest request) {
        return knowledgeDocumentService.update(restaurantProperties.getDefaultId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER')")
    public void delete(@PathVariable UUID id) {
        knowledgeDocumentService.delete(restaurantProperties.getDefaultId(), id);
    }
}
