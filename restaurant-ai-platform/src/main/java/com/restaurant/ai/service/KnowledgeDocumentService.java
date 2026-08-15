package com.restaurant.ai.service;

import com.restaurant.ai.dto.KnowledgeDocumentRequest;
import com.restaurant.ai.dto.KnowledgeDocumentResponse;
import com.restaurant.ai.entity.KnowledgeDocument;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.repository.KnowledgeDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class KnowledgeDocumentService {

    private static final int MAX_SEARCH_RESULTS = 3;

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    public KnowledgeDocumentService(KnowledgeDocumentRepository knowledgeDocumentRepository) {
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
    }

    public List<KnowledgeDocumentResponse> search(UUID restaurantId, String query) {
        return knowledgeDocumentRepository.searchByText(restaurantId, query, MAX_SEARCH_RESULTS).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<KnowledgeDocumentResponse> listAll(UUID restaurantId) {
        return knowledgeDocumentRepository.findByRestaurantIdAndActiveTrue(restaurantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public KnowledgeDocumentResponse create(UUID restaurantId, KnowledgeDocumentRequest request) {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setRestaurantId(restaurantId);
        doc.setTitle(request.title());
        doc.setCategory(request.category());
        doc.setContent(request.content());
        doc.setActive(request.active() == null || request.active());
        knowledgeDocumentRepository.save(doc);
        return toResponse(doc);
    }

    @Transactional
    public KnowledgeDocumentResponse update(UUID restaurantId, UUID id, KnowledgeDocumentRequest request) {
        KnowledgeDocument doc = knowledgeDocumentRepository.findById(id)
                .filter(d -> d.getRestaurantId().equals(restaurantId))
                .orElseThrow(() -> ResourceNotFoundException.of("KnowledgeDocument", id));
        doc.setTitle(request.title());
        doc.setCategory(request.category());
        doc.setContent(request.content());
        if (request.active() != null) {
            doc.setActive(request.active());
        }
        return toResponse(doc);
    }

    @Transactional
    public void delete(UUID restaurantId, UUID id) {
        KnowledgeDocument doc = knowledgeDocumentRepository.findById(id)
                .filter(d -> d.getRestaurantId().equals(restaurantId))
                .orElseThrow(() -> ResourceNotFoundException.of("KnowledgeDocument", id));
        knowledgeDocumentRepository.delete(doc);
    }

    private KnowledgeDocumentResponse toResponse(KnowledgeDocument d) {
        return new KnowledgeDocumentResponse(d.getId(), d.getTitle(), d.getCategory(), d.getContent());
    }
}
