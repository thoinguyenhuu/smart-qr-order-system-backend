package com.ht.project.smart_qr_menu_order_system.dtos;

import lombok.Data;

import java.util.List;

@Data
public class MenuItemDTO{
    private String category;
    private List<ItemDTO> items ;
}

