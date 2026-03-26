package com.ht.project.smart_qr_menu_order_system.service.impl;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateTableRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateTableRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.TableResponse;
import com.ht.project.smart_qr_menu_order_system.entity.Restaurant;
import com.ht.project.smart_qr_menu_order_system.entity.Tables;
import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import com.ht.project.smart_qr_menu_order_system.repository.TablesRepository;
import com.ht.project.smart_qr_menu_order_system.service.RestaurantAccessService;
import com.ht.project.smart_qr_menu_order_system.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Table service manages table metadata and QR target links.
 */
@Service
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {
    private final TablesRepository tablesRepository;
    private final RestaurantAccessService restaurantAccessService;
    private final QrCodeService qrCodeService;

    @Value("${app.qr.base_url}")
    private String qrBaseUrl;

    @Override
    @Transactional
    public TableResponse create(String restaurantId, CreateTableRequest request, String userId) {
        Restaurant restaurant = restaurantAccessService.getRestaurantForOwner(restaurantId, userId);
        validateDuplicateTableNumber(restaurantId, request.tableNumber());

        Tables table = Tables.builder()
                .restaurant(restaurant)
                .tableNumber(request.tableNumber())
                .available(request.available())
                .build();
        table.setTenantId(restaurantId);
        Tables savedTable = tablesRepository.save(table);
        savedTable.setQrCode(buildQrPath(restaurantId, savedTable.getId()));
        return map(tablesRepository.save(savedTable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TableResponse> getAll(String restaurantId, String userId) {
        restaurantAccessService.getRestaurantForMember(restaurantId, userId);
        return tablesRepository.findAllByRestaurantIdOrderByTableNumberAsc(restaurantId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TableResponse getById(String restaurantId, String tableId, String userId) {
        restaurantAccessService.getRestaurantForMember(restaurantId, userId);
        return map(findTable(restaurantId, tableId));
    }

    @Override
    @Transactional(readOnly = true)
    public TableResponse getPublicTable(String restaurantId, String tableId) {
        return map(findTable(restaurantId, tableId));
    }

    @Override
    @Transactional
    public TableResponse update(String restaurantId, String tableId, UpdateTableRequest request, String userId) {
        restaurantAccessService.getRestaurantForOwner(restaurantId, userId);
        Tables table = findTable(restaurantId, tableId);
        if (table.getTableNumber() != request.tableNumber()) {
            validateDuplicateTableNumber(restaurantId, request.tableNumber());
        }
        table.setTableNumber(request.tableNumber());
        table.setAvailable(request.available());
        table.setQrCode(buildQrPath(restaurantId, table.getId()));
        return map(tablesRepository.save(table));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateQrCode(String restaurantId, String tableId, String userId) {
        restaurantAccessService.getRestaurantForMember(restaurantId, userId);
        Tables table = findTable(restaurantId, tableId);
        return qrCodeService.generatePng(resolveQrTargetUrl(table.getRestaurant().getId(), table.getId()));
    }

    @Override
    @Transactional
    public void delete(String restaurantId, String tableId, String userId) {
        restaurantAccessService.getRestaurantForOwner(restaurantId, userId);
        tablesRepository.delete(findTable(restaurantId, tableId));
    }

    private Tables findTable(String restaurantId, String tableId) {
        return tablesRepository.findByIdAndRestaurantId(tableId, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.TABLE_NOT_FOUND));
    }

    private void validateDuplicateTableNumber(String restaurantId, int tableNumber) {
        if (tablesRepository.existsByRestaurantIdAndTableNumber(restaurantId, tableNumber)) {
            throw new AppException(AppErrorCode.BAD_REQUEST, "Table number already exists");
        }
    }

    private String buildQrPath(String restaurantId, String tableId) {
        return resolveQrTargetUrl(restaurantId, tableId);
    }

    private String resolveQrTargetUrl(String restaurantId, String tableId) {
        return normalizeBaseUrl(qrBaseUrl) + "/scan/" + restaurantId + "/" + tableId;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private TableResponse map(Tables table) {
        return new TableResponse(
                table.getId(),
                table.getRestaurant().getId(),
                table.getTableNumber(),
                table.getQrCode(),
                Boolean.TRUE.equals(table.getAvailable())
        );
    }
}
