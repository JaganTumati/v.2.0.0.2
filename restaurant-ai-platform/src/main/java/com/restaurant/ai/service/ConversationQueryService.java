package com.restaurant.ai.service;

import com.restaurant.ai.dto.ConversationResponse;
import com.restaurant.ai.dto.MessageResponse;
import com.restaurant.ai.entity.Conversation;
import com.restaurant.ai.entity.Customer;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.mapper.ConversationMapper;
import com.restaurant.ai.repository.ConversationRepository;
import com.restaurant.ai.repository.CustomerRepository;
import com.restaurant.ai.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConversationQueryService {

    private final ConversationRepository conversationRepository;
    private final CustomerRepository customerRepository;
    private final MessageRepository messageRepository;

    public ConversationQueryService(ConversationRepository conversationRepository,
                                     CustomerRepository customerRepository,
                                     MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.customerRepository = customerRepository;
        this.messageRepository = messageRepository;
    }

    public List<ConversationResponse> listByMode(UUID restaurantId, String mode) {
        List<Conversation> conversations = mode != null
                ? conversationRepository.findByRestaurantIdAndModeOrderByUpdatedAtDesc(restaurantId, mode)
                : conversationRepository.findByRestaurantIdOrderByUpdatedAtDesc(restaurantId);

        return conversations.stream()
                .map(c -> ConversationMapper.toResponse(c, customerRepository.findById(c.getCustomerId()).orElse(null)))
                .toList();
    }

    public ConversationResponse get(UUID restaurantId, UUID conversationId) {
        Conversation conversation = conversationRepository.findByIdAndRestaurantId(conversationId, restaurantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conversation", conversationId));
        Customer customer = customerRepository.findById(conversation.getCustomerId()).orElse(null);
        return ConversationMapper.toResponse(conversation, customer);
    }

    public List<MessageResponse> getMessages(UUID restaurantId, UUID conversationId) {
        // Confirms the conversation belongs to this restaurant before returning its messages.
        conversationRepository.findByIdAndRestaurantId(conversationId, restaurantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conversation", conversationId));

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(ConversationMapper::toResponse)
                .toList();
    }
}
