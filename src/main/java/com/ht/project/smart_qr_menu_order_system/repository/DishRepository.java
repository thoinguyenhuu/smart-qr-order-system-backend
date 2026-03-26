package com.ht.project.smart_qr_menu_order_system.repository;

import com.ht.project.smart_qr_menu_order_system.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, String> {
    List<Dish> findAllByCategoryIdInAndAvailableTrue(List<String> categoryIds);

    Optional<Dish> findByIdAndCategoryRestaurantId(String id, String restaurantId);

    List<Dish> findAllByCategoryRestaurantIdOrderByNameAsc(String restaurantId);

    Optional<Dish> findByIdAndCategoryIdAndCategoryRestaurantId(String id, String categoryId, String restaurantId);
}
