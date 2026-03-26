package com.ht.project.smart_qr_menu_order_system.repository;

import com.ht.project.smart_qr_menu_order_system.entity.RestaurantUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantUserRepository extends JpaRepository<RestaurantUser, String> {
    boolean existsByRestaurantIdAndUserId(String restaurantId, String userId);

    Optional<RestaurantUser> findByRestaurantIdAndUserId(String restaurantId, String userId);

    @EntityGraph(attributePaths = "user")
    List<RestaurantUser> findAllByRestaurantId(String restaurantId);

    long countByUserId(String userId);

    @EntityGraph(attributePaths = "restaurant")
    List<RestaurantUser> findAllByUserId(String userId);
}
