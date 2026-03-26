package com.ht.project.smart_qr_menu_order_system.repository;

import com.ht.project.smart_qr_menu_order_system.entity.Tables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TablesRepository extends JpaRepository<Tables, String> {
    Optional<Tables> findByIdAndRestaurantId(String id, String restaurantId);

    Optional<Tables> findByRestaurantIdAndTableNumber(String restaurantId, int tableNumber);

    List<Tables> findAllByRestaurantIdOrderByTableNumberAsc(String restaurantId);

    boolean existsByRestaurantIdAndTableNumber(String restaurantId, int tableNumber);
}
