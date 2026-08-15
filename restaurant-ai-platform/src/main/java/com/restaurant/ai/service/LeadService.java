package com.restaurant.ai.service;

import com.restaurant.ai.dto.LeadResponse;
import com.restaurant.ai.entity.Lead;
import com.restaurant.ai.repository.LeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LeadService {

    private final LeadRepository leadRepository;

    public LeadService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    public LeadResponse create(UUID restaurantId, UUID customerId, UUID conversationId,
                                String source, String intentNotes, BigDecimal estimatedValue) {
        Lead lead = new Lead();
        lead.setRestaurantId(restaurantId);
        lead.setCustomerId(customerId);
        lead.setConversationId(conversationId);
        lead.setSource(source);
        lead.setIntentNotes(intentNotes);
        lead.setEstimatedValue(estimatedValue);
        lead.setStatus("NEW");
        leadRepository.save(lead);
        return toResponse(lead);
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> list(UUID restaurantId) {
        return leadRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(this::toResponse)
                .toList();
    }

    private LeadResponse toResponse(Lead l) {
        return new LeadResponse(l.getId(), l.getSource(), l.getIntentNotes(), l.getEstimatedValue(), l.getStatus(), l.getCreatedAt());
    }
}
