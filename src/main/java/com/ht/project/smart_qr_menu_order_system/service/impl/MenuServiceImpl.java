package com.ht.project.smart_qr_menu_order_system.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ht.project.smart_qr_menu_order_system.dtos.ItemDTO;
import com.ht.project.smart_qr_menu_order_system.dtos.MenuItemDTO;
import com.ht.project.smart_qr_menu_order_system.dtos.response.MenuCategoryResponse;
import com.ht.project.smart_qr_menu_order_system.dtos.response.MenuDishResponse;
import com.ht.project.smart_qr_menu_order_system.dtos.response.MenuResponse;
import com.ht.project.smart_qr_menu_order_system.entity.Category;
import com.ht.project.smart_qr_menu_order_system.entity.Dish;
import com.ht.project.smart_qr_menu_order_system.entity.Restaurant;
import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import com.ht.project.smart_qr_menu_order_system.repository.CategoryRepository;
import com.ht.project.smart_qr_menu_order_system.repository.DishRepository;
import com.ht.project.smart_qr_menu_order_system.repository.RestaurantRepository;
import com.ht.project.smart_qr_menu_order_system.service.AIService;
import com.ht.project.smart_qr_menu_order_system.service.MenuService;
import com.ht.project.smart_qr_menu_order_system.service.RestaurantAccessService;
import com.ht.project.smart_qr_menu_order_system.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String IMPORT_PROMPT = """
            Extract all dishes from this menu image.

            Return strictly in JSON:
            [
                {
                    "category" : string,
                    "items" : [
                        {
                            "name" : string,
                            "price" : number
                        },
                        {
                            "name" : string,
                            "price" : number
                        }
                    ]
                },
                {
                    "category" : string,
                    "items" : [
                        {
                            "name" : string,
                            "price" : number
                        },
                        {
                            "name" : string,
                            "price" : number
                        }
                    ]
                }
            ]

            Only return JSON.
            Return price as integer (no dot, no currency symbol)
            """;

    private final AIService aiService;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;
    private final RestaurantAccessService restaurantAccessService;

    @Override
    @Transactional
    public void generateMenuFromImage(String restaurantId, MultipartFile image) throws IOException {
        String userId = SecurityUtils.getCurrentUserId();
        Restaurant restaurant = restaurantAccessService.getRestaurantForOwner(restaurantId, userId);

        List<MenuItemDTO> parsedMenu = parseMenu(aiService.generateText(image, IMPORT_PROMPT));
        List<Category> existingCategories = categoryRepository.findAllByRestaurantIdOrderByDisplayOrderAsc(restaurantId);
        List<Dish> existingDishes = dishRepository.findAllByCategoryRestaurantIdOrderByNameAsc(restaurantId);

        Map<String, Category> categoriesByNormalizedName = existingCategories.stream()
                .filter(category -> !normalizeCategoryName(category.getName()).isBlank())
                .collect(Collectors.toMap(
                        category -> normalizeCategoryName(category.getName()),
                        Function.identity(),
                        (first, second) -> first,
                        HashMap::new
                ));

        Map<String, Set<String>> dishNamesByCategoryId = existingDishes.stream()
                .collect(Collectors.groupingBy(
                        dish -> dish.getCategory().getId(),
                        HashMap::new,
                        Collectors.mapping(
                                dish -> normalizeDishName(dish.getName()),
                                Collectors.toCollection(HashSet::new)
                        )
                ));

        int nextDisplayOrder = existingCategories.stream()
                .mapToInt(Category::getDisplayOrder)
                .max()
                .orElse(-1) + 1;

        List<Dish> dishesToCreate = new ArrayList<>();

        for (MenuItemDTO categoryItem : parsedMenu) {
            String normalizedCategoryName = normalizeCategoryName(categoryItem.getCategory());
            if (normalizedCategoryName.isBlank()) {
                continue;
            }

            Category category = categoriesByNormalizedName.get(normalizedCategoryName);
            if (category == null) {
                category = createCategory(restaurant, categoryItem.getCategory().trim(), nextDisplayOrder++);
                categoriesByNormalizedName.put(normalizedCategoryName, category);
            }

            Set<String> existingDishNames = dishNamesByCategoryId.computeIfAbsent(
                    category.getId(),
                    ignored -> new HashSet<>()
            );

            for (ItemDTO item : safeItems(categoryItem)) {
                String normalizedDishName = normalizeDishName(item.getName());
                if (normalizedDishName.isBlank()) {
                    continue;
                }
                if (existingDishNames.contains(normalizedDishName)) {
                    log.info(
                            "Skip duplicated dish '{}' in category '{}' for restaurant '{}'",
                            item.getName(),
                            category.getName(),
                            restaurantId
                    );
                    continue;
                }

                dishesToCreate.add(createDish(restaurantId, category, item));
                existingDishNames.add(normalizedDishName);
            }
        }

        if (!dishesToCreate.isEmpty()) {
            dishRepository.saveAll(dishesToCreate);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse getMenuOfRestaurant(String restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.RESTAURANT_NOT_FOUND));

        List<Category> categories = categoryRepository.findAllByRestaurantIdOrderByDisplayOrderAsc(restaurantId);
        List<String> categoryIds = categories.stream().map(Category::getId).toList();
        List<Dish> dishes = categoryIds.isEmpty()
                ? Collections.emptyList()
                : dishRepository.findAllByCategoryIdInAndAvailableTrue(categoryIds);

        Map<String, List<Dish>> dishesByCategoryId = dishes.stream()
                .collect(Collectors.groupingBy(dish -> dish.getCategory().getId()));

        List<MenuCategoryResponse> menuCategories = categories.stream()
                .map(category -> new MenuCategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDisplayOrder(),
                        dishesByCategoryId.getOrDefault(category.getId(), Collections.emptyList())
                                .stream()
                                .map(mapDish())
                                .toList()
                ))
                .toList();

        return new MenuResponse(restaurant.getId(), restaurant.getName(), menuCategories);
    }

    private Category createCategory(Restaurant restaurant, String name, int displayOrder) {
        Category category = Category.builder()
                .restaurant(restaurant)
                .name(name)
                .displayOrder(displayOrder)
                .build();
        category.setTenantId(restaurant.getId());
        return categoryRepository.save(category);
    }

    private Dish createDish(String restaurantId, Category category, ItemDTO item) {
        Dish dish = Dish.builder()
                .category(category)
                .name(item.getName().trim())
                .description(null)
                .price(item.getPrice())
                .imageUrl(null)
                .available(true)
                .soldOutOn(null)
                .build();
        dish.setTenantId(restaurantId);
        return dish;
    }

    private List<ItemDTO> safeItems(MenuItemDTO menuItem) {
        return menuItem.getItems() == null ? Collections.emptyList() : menuItem.getItems();
    }

    private Function<Dish, MenuDishResponse> mapDish() {
        return dish -> new MenuDishResponse(
                dish.getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getPrice(),
                dish.getImageUrl(),
                dish.isAvailableForOrder(BUSINESS_ZONE),
                dish.isSoldOutToday(BUSINESS_ZONE)
        );
    }

    private List<MenuItemDTO> parseMenu(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, new TypeReference<List<MenuItemDTO>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Parse JSON failed", e);
        }
    }

    private String normalizeCategoryName(String value) {
        return normalize(value);
    }

    private String normalizeDishName(String value) {
        return normalize(value);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
