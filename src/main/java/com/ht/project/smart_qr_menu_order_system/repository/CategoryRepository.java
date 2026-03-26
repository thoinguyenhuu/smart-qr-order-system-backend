package com.ht.project.smart_qr_menu_order_system.repository;

import com.ht.project.smart_qr_menu_order_system.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findAllByRestaurantIdOrderByDisplayOrderAsc(String restaurantId);

    Optional<Category> findByIdAndRestaurantId(String id, String restaurantId);

    Optional<Category> findByNameAndRestaurantId(String name, String restaurantId);
}
