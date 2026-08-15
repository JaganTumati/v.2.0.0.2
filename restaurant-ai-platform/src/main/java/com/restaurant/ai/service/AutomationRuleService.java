package com.restaurant.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.ai.dto.AutomationRuleRequest;
import com.restaurant.ai.dto.AutomationRuleResponse;
import com.restaurant.ai.entity.AutomationRule;
import com.restaurant.ai.exception.BusinessRuleViolationException;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.repository.AutomationRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AutomationRuleService {

    private final AutomationRuleRepository automationRuleRepository;
    private final ObjectMapper objectMapper;

    public AutomationRuleService(AutomationRuleRepository automationRuleRepository, ObjectMapper objectMapper) {
        this.automationRuleRepository = automationRuleRepository;
        this.objectMapper = objectMapper;
    }

    public List<AutomationRuleResponse> list(UUID restaurantId) {
        return automationRuleRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AutomationRuleResponse create(UUID restaurantId, AutomationRuleRequest request) {
        AutomationRule rule = new AutomationRule();
        rule.setRestaurantId(restaurantId);
        rule.setName(request.name());
        rule.setTriggerType(request.triggerType());
        rule.setConditionJson(writeJson(request.condition()));
        rule.setActionType(request.actionType());
        rule.setActionConfigJson(writeJson(request.actionConfig()));
        rule.setActive(request.active() == null || request.active());
        automationRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public AutomationRuleResponse update(UUID restaurantId, UUID id, AutomationRuleRequest request) {
        AutomationRule rule = automationRuleRepository.findById(id)
                .filter(r -> r.getRestaurantId().equals(restaurantId))
                .orElseThrow(() -> ResourceNotFoundException.of("AutomationRule", id));
        rule.setName(request.name());
        rule.setTriggerType(request.triggerType());
        rule.setConditionJson(writeJson(request.condition()));
        rule.setActionType(request.actionType());
        rule.setActionConfigJson(writeJson(request.actionConfig()));
        if (request.active() != null) {
            rule.setActive(request.active());
        }
        return toResponse(rule);
    }

    @Transactional
    public void delete(UUID restaurantId, UUID id) {
        AutomationRule rule = automationRuleRepository.findById(id)
                .filter(r -> r.getRestaurantId().equals(restaurantId))
                .orElseThrow(() -> ResourceNotFoundException.of("AutomationRule", id));
        automationRuleRepository.delete(rule);
    }

    private String writeJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Invalid condition/action configuration.");
        }
    }

    private AutomationRuleResponse toResponse(AutomationRule rule) {
        return new AutomationRuleResponse(
                rule.getId(), rule.getName(), rule.getTriggerType(),
                readJson(rule.getConditionJson()), rule.getActionType(),
                readJson(rule.getActionConfigJson()), rule.isActive());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJson(String json) {
        try {
            return json == null ? Map.of() : objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}
