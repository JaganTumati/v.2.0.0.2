package com.restaurant.ai.service;

import com.restaurant.ai.dto.SupportTicketResponse;
import com.restaurant.ai.entity.SupportTicket;
import com.restaurant.ai.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    public SupportTicketService(SupportTicketRepository supportTicketRepository) {
        this.supportTicketRepository = supportTicketRepository;
    }

    public SupportTicketResponse create(UUID restaurantId, UUID conversationId, UUID orderId, String issue, String priority) {
        SupportTicket ticket = new SupportTicket();
        ticket.setRestaurantId(restaurantId);
        ticket.setConversationId(conversationId);
        ticket.setOrderId(orderId);
        ticket.setIssue(issue);
        ticket.setPriority(priority == null || priority.isBlank() ? "NORMAL" : priority.toUpperCase());
        ticket.setStatus("OPEN");
        supportTicketRepository.save(ticket);
        return toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponse> list(UUID restaurantId) {
        return supportTicketRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(this::toResponse)
                .toList();
    }

    private SupportTicketResponse toResponse(SupportTicket t) {
        return new SupportTicketResponse(t.getId(), t.getConversationId(), t.getOrderId(), t.getIssue(), t.getPriority(), t.getStatus(), t.getCreatedAt());
    }
}
