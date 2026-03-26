package com.ht.project.smart_qr_menu_order_system.service;

import com.ht.project.smart_qr_menu_order_system.dtos.response.MenuResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MenuService {
    void generateMenuFromImage(String restaurantId, MultipartFile image) throws IOException;
    MenuResponse getMenuOfRestaurant(String restaurantId);
}
