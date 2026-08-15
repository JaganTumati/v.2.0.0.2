package com.restaurant.ai.service;

import com.restaurant.ai.dto.DeliveryZoneResponse;
import com.restaurant.ai.entity.DeliveryZone;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.repository.DeliveryZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DeliveryZoneService {

    private final DeliveryZoneRepository deliveryZoneRepository;

    public DeliveryZoneService(DeliveryZoneRepository deliveryZoneRepository) {
        this.deliveryZoneRepository = deliveryZoneRepository;
    }

    /** Matches a postal code against active zones. Returns empty if not serviceable. */
    public Optional<DeliveryZoneResponse> findZoneForPostalCode(UUID restaurantId, String postalCode) {
        return deliveryZoneRepository.findByRestaurantIdAndActiveTrue(restaurantId).stream()
                .filter(z -> z.getPostalCodes() != null && z.getPostalCodes().contains(postalCode.trim()))
                .findFirst()
                .map(this::toResponse);
    }

    public DeliveryZone getOrThrow(UUID restaurantId, UUID zoneId) {
        DeliveryZone zone = deliveryZoneRepository.findById(zoneId)
                .orElseThrow(() -> ResourceNotFoundException.of("DeliveryZone", zoneId));
        if (!zone.getRestaurantId().equals(restaurantId)) {
            throw ResourceNotFoundException.of("DeliveryZone", zoneId);
        }
        return zone;
    }

    public List<DeliveryZoneResponse> listActive(UUID restaurantId) {
        return deliveryZoneRepository.findByRestaurantIdAndActiveTrue(restaurantId).stream()
                .map(this::toResponse)
                .toList();
    }

    private DeliveryZoneResponse toResponse(DeliveryZone z) {
        return new DeliveryZoneResponse(z.getId(), z.getName(), z.getDeliveryFee(), z.getMinOrderAmount(), z.getEstimatedMinutes());
    }
}
