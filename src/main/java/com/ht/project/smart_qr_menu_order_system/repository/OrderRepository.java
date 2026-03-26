package com.ht.project.smart_qr_menu_order_system.repository;

import com.ht.project.smart_qr_menu_order_system.entity.Order;
import com.ht.project.smart_qr_menu_order_system.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @EntityGraph(attributePaths = "restaurant")
    Optional<Order> findByIdAndRestaurantId(String id, String restaurantId);

    @EntityGraph(attributePaths = "restaurant")
    List<Order> findAllByRestaurantIdOrderByCreatedAtDesc(String restaurantId);

    @EntityGraph(attributePaths = "restaurant")
    List<Order> findAllByRestaurantIdAndTableNumberAndStatusInOrderByCreatedAtDesc(
            String restaurantId,
            int tableNumber,
            List<OrderStatus> statuses
    );

    @EntityGraph(attributePaths = "restaurant")
    Optional<Order> findById(String id);
}
