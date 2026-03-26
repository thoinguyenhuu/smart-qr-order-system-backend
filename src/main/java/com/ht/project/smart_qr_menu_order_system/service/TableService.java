package com.ht.project.smart_qr_menu_order_system.service;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateTableRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateTableRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.TableResponse;

import java.util.List;

public interface TableService {
    TableResponse create(String restaurantId, CreateTableRequest request, String userId);

    List<TableResponse> getAll(String restaurantId, String userId);

    TableResponse getById(String restaurantId, String tableId, String userId);

    TableResponse getPublicTable(String restaurantId, String tableId);

    TableResponse update(String restaurantId, String tableId, UpdateTableRequest request, String userId);

    byte[] generateQrCode(String restaurantId, String tableId, String userId);

    void delete(String restaurantId, String tableId, String userId);
}
