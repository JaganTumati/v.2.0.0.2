package com.restaurant.ai.service;

import com.restaurant.ai.entity.Conversation;
import com.restaurant.ai.entity.Customer;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.integration.WhatsAppClient;
import com.restaurant.ai.repository.ConversationRepository;
import com.restaurant.ai.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AgentReplyService {

    private final ConversationRepository conversationRepository;
    private final CustomerRepository customerRepository;
    private final ConversationService conversationService;
    private final WhatsAppClient whatsAppClient;

    public AgentReplyService(ConversationRepository conversationRepository, CustomerRepository customerRepository,
                              ConversationService conversationService, WhatsAppClient whatsAppClient) {
        this.conversationRepository = conversationRepository;
        this.customerRepository = customerRepository;
        this.conversationService = conversationService;
        this.whatsAppClient = whatsAppClient;
    }

    public void sendAgentReply(UUID restaurantId, UUID conversationId, UUID agentUserId, String content) {
        Conversation conversation = conversationRepository.findByIdAndRestaurantId(conversationId, restaurantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conversation", conversationId));
        Customer customer = customerRepository.findById(conversation.getCustomerId())
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", conversation.getCustomerId()));

        conversationService.appendMessage(conversationId, "OUTBOUND", "AGENT", content, null, agentUserId);
        whatsAppClient.sendTextMessage(customer.getWhatsappNumber(), content);
    }
}
