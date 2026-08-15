package com.restaurant.ai.mapper;

import com.restaurant.ai.dto.ConversationResponse;
import com.restaurant.ai.dto.MessageResponse;
import com.restaurant.ai.entity.Conversation;
import com.restaurant.ai.entity.Customer;
import com.restaurant.ai.entity.Message;

public final class ConversationMapper {

    private ConversationMapper() {
    }

    public static ConversationResponse toResponse(Conversation c, Customer customer) {
        return new ConversationResponse(
                c.getId(),
                c.getCustomerId(),
                customer != null ? customer.getDisplayName() : null,
                customer != null ? customer.getWhatsappNumber() : null,
                c.getMode(),
                c.getLastIntent(),
                c.getSummary(),
                c.getUpdatedAt()
        );
    }

    public static MessageResponse toResponse(Message m) {
        return new MessageResponse(m.getId(), m.getDirection(), m.getSenderType(), m.getContent(), m.getCreatedAt());
    }
}
