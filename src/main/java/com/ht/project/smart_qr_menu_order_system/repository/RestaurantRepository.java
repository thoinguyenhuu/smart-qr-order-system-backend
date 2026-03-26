package com.ht.project.smart_qr_menu_order_system.repository;

import com.ht.project.smart_qr_menu_order_system.entity.Restaurant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, String> {
    @EntityGraph(attributePaths = "owner")
    Optional<Restaurant> findByIdAndOwnerId(String id, String ownerId);
}
