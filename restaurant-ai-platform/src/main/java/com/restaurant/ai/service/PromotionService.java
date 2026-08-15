package com.restaurant.ai.service;

import com.restaurant.ai.dto.PromotionResponse;
import com.restaurant.ai.entity.Promotion;
import com.restaurant.ai.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public List<PromotionResponse> listCurrentlyValid(UUID restaurantId) {
        return promotionRepository.findByRestaurantIdAndActiveTrue(restaurantId).stream()
                .filter(Promotion::isCurrentlyValid)
                .map(this::toResponse)
                .toList();
    }

    /** Returns empty if the code doesn't exist OR exists but isn't currently valid - callers must not distinguish these to avoid leaking which codes exist. */
    public Optional<Promotion> findValidByCode(UUID restaurantId, String code) {
        return promotionRepository.findByRestaurantIdAndCodeIgnoreCase(restaurantId, code)
                .filter(Promotion::isCurrentlyValid);
    }

    private PromotionResponse toResponse(Promotion p) {
        return new PromotionResponse(p.getCode(), p.getDescription(), p.getDiscountType(), p.getDiscountValue(), p.getValidUntil());
    }
}
