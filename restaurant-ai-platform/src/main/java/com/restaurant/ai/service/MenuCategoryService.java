package com.restaurant.ai.service;

import com.restaurant.ai.dto.MenuCategoryRequest;
import com.restaurant.ai.dto.MenuCategoryResponse;
import com.restaurant.ai.entity.MenuCategory;
import com.restaurant.ai.exception.BusinessRuleViolationException;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.mapper.MenuMapper;
import com.restaurant.ai.repository.MenuCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;

    public MenuCategoryService(MenuCategoryRepository menuCategoryRepository) {
        this.menuCategoryRepository = menuCategoryRepository;
    }

    public List<MenuCategoryResponse> list(UUID restaurantId) {
        return menuCategoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(restaurantId).stream()
                .map(MenuMapper::toResponse)
                .toList();
    }

    @Transactional
    public MenuCategoryResponse create(UUID restaurantId, MenuCategoryRequest request) {
        if (menuCategoryRepository.existsByRestaurantIdAndNameIgnoreCase(restaurantId, request.name())) {
            throw new BusinessRuleViolationException("A category named '" + request.name() + "' already exists.");
        }
        MenuCategory category = new MenuCategory();
        category.setRestaurantId(restaurantId);
        category.setName(request.name());
        category.setDisplayOrder(request.displayOrder());
        category.setActive(request.active() == null || request.active());
        menuCategoryRepository.save(category);
        return MenuMapper.toResponse(category);
    }

    @Transactional
    public MenuCategoryResponse update(UUID restaurantId, UUID categoryId, MenuCategoryRequest request) {
        MenuCategory category = findOrThrow(restaurantId, categoryId);
        category.setName(request.name());
        category.setDisplayOrder(request.displayOrder());
        if (request.active() != null) {
            category.setActive(request.active());
        }
        return MenuMapper.toResponse(category);
    }

    @Transactional
    public void delete(UUID restaurantId, UUID categoryId) {
        MenuCategory category = findOrThrow(restaurantId, categoryId);
        menuCategoryRepository.delete(category);
    }

    private MenuCategory findOrThrow(UUID restaurantId, UUID categoryId) {
        return menuCategoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> ResourceNotFoundException.of("MenuCategory", categoryId));
    }
}
