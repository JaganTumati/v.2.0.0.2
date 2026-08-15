package com.restaurant.ai.service;

import com.restaurant.ai.dto.MenuItemRequest;
import com.restaurant.ai.dto.MenuItemResponse;
import com.restaurant.ai.entity.Inventory;
import com.restaurant.ai.entity.MenuCategory;
import com.restaurant.ai.entity.MenuItem;
import com.restaurant.ai.entity.MenuItemOption;
import com.restaurant.ai.exception.BusinessRuleViolationException;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.mapper.MenuMapper;
import com.restaurant.ai.repository.InventoryRepository;
import com.restaurant.ai.repository.MenuCategoryRepository;
import com.restaurant.ai.repository.MenuItemOptionRepository;
import com.restaurant.ai.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemOptionRepository menuItemOptionRepository;
    private final InventoryRepository inventoryRepository;

    public MenuItemService(MenuItemRepository menuItemRepository,
                            MenuCategoryRepository menuCategoryRepository,
                            MenuItemOptionRepository menuItemOptionRepository,
                            InventoryRepository inventoryRepository) {
        this.menuItemRepository = menuItemRepository;
        this.menuCategoryRepository = menuCategoryRepository;
        this.menuItemOptionRepository = menuItemOptionRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<MenuItemResponse> list(UUID restaurantId, UUID categoryId, boolean includeUnavailable) {
        List<MenuItem> items = categoryId != null
                ? menuItemRepository.findByRestaurantIdAndCategoryId(restaurantId, categoryId)
                : (includeUnavailable
                    ? menuItemRepository.findByRestaurantId(restaurantId)
                    : menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId));
        return toResponses(restaurantId, items);
    }

    /**
     * Filtered search. This is the exact query shape the future search_menu
     * Claude tool will call through — always restaurant-scoped, only ever
     * returns available items so Claude can never surface something it
     * shouldn't offer.
     */
    public List<MenuItemResponse> search(UUID restaurantId, UUID categoryId, Boolean vegetarian,
                                          BigDecimal maxPrice, Short maxSpicyLevel, String searchText) {
        List<MenuItem> items = menuItemRepository.search(
                restaurantId, categoryId, vegetarian, maxPrice, maxSpicyLevel, searchText, false);
        return toResponses(restaurantId, items);
    }

    public MenuItemResponse get(UUID restaurantId, UUID itemId) {
        MenuItem item = findOrThrow(restaurantId, itemId);
        return toResponse(item);
    }

    public boolean checkAvailability(UUID restaurantId, UUID itemId) {
        MenuItem item = findOrThrow(restaurantId, itemId);
        return item.isAvailable();
    }

    @Transactional
    public MenuItemResponse create(UUID restaurantId, MenuItemRequest request) {
        MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(request.categoryId(), restaurantId)
                .orElseThrow(() -> ResourceNotFoundException.of("MenuCategory", request.categoryId()));

        MenuItem item = new MenuItem();
        applyRequest(item, request);
        item.setRestaurantId(restaurantId);
        menuItemRepository.save(item);

        Inventory inventory = new Inventory();
        inventory.setMenuItemId(item.getId());
        inventory.setTrackStock(false);
        inventoryRepository.save(inventory);

        return MenuMapper.toResponse(item, category.getName(), List.of());
    }

    @Transactional
    public MenuItemResponse update(UUID restaurantId, UUID itemId, MenuItemRequest request) {
        MenuItem item = findOrThrow(restaurantId, itemId);
        MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(request.categoryId(), restaurantId)
                .orElseThrow(() -> ResourceNotFoundException.of("MenuCategory", request.categoryId()));
        applyRequest(item, request);
        List<MenuItemOption> options = menuItemOptionRepository.findByMenuItemId(item.getId());
        return MenuMapper.toResponse(item, category.getName(), options);
    }

    @Transactional
    public MenuItemResponse setAvailability(UUID restaurantId, UUID itemId, boolean available) {
        MenuItem item = findOrThrow(restaurantId, itemId);
        item.setAvailable(available);
        return toResponse(item);
    }

    @Transactional
    public void delete(UUID restaurantId, UUID itemId) {
        MenuItem item = findOrThrow(restaurantId, itemId);
        menuItemRepository.delete(item);
    }

    private void applyRequest(MenuItem item, MenuItemRequest request) {
        item.setCategoryId(request.categoryId());
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setVegetarian(request.vegetarian());
        item.setSpicyLevel(request.spicyLevel() == null ? 0 : request.spicyLevel());
        item.setAvailable(request.available() == null || request.available());
        item.setImageUrl(request.imageUrl());
        if (request.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("Price cannot be negative.");
        }
    }

    private MenuItem findOrThrow(UUID restaurantId, UUID itemId) {
        return menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> ResourceNotFoundException.of("MenuItem", itemId));
    }

    private MenuItemResponse toResponse(MenuItem item) {
        MenuCategory category = menuCategoryRepository.findById(item.getCategoryId()).orElse(null);
        List<MenuItemOption> options = menuItemOptionRepository.findByMenuItemIdAndActiveTrue(item.getId());
        return MenuMapper.toResponse(item, category != null ? category.getName() : null, options);
    }

    private List<MenuItemResponse> toResponses(UUID restaurantId, List<MenuItem> items) {
        if (items.isEmpty()) {
            return List.of();
        }
        Map<UUID, String> categoryNames = menuCategoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(restaurantId)
                .stream()
                .collect(java.util.stream.Collectors.toMap(MenuCategory::getId, MenuCategory::getName));
        return items.stream()
                .map(item -> MenuMapper.toResponse(
                        item,
                        categoryNames.get(item.getCategoryId()),
                        menuItemOptionRepository.findByMenuItemIdAndActiveTrue(item.getId())))
                .toList();
    }
}
