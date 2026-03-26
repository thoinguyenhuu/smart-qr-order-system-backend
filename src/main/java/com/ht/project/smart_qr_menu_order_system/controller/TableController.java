package com.ht.project.smart_qr_menu_order_system.controller;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateTableRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateTableRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.TableResponse;
import com.ht.project.smart_qr_menu_order_system.service.TableService;
import com.ht.project.smart_qr_menu_order_system.utils.ApiResult;
import com.ht.project.smart_qr_menu_order_system.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Table management APIs and public QR target API.
 */
@RestController
@RequestMapping("/restaurants/{restaurantId}/tables")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TableController {
    TableService tableService;

    @PostMapping
    ApiResult<TableResponse> create(@PathVariable String restaurantId, @Valid @RequestBody CreateTableRequest request) {
        return ApiResult.<TableResponse>builder()
                .data(tableService.create(restaurantId, request, SecurityUtils.getCurrentUserId()))
                .message("Table created successfully")
                .build();
    }

    @GetMapping
    ApiResult<List<TableResponse>> getAll(@PathVariable String restaurantId) {
        return ApiResult.<List<TableResponse>>builder()
                .data(tableService.getAll(restaurantId, SecurityUtils.getCurrentUserId()))
                .build();
    }

    @GetMapping("/{tableId}")
    ApiResult<TableResponse> getById(@PathVariable String restaurantId, @PathVariable String tableId) {
        return ApiResult.<TableResponse>builder()
                .data(tableService.getById(restaurantId, tableId, SecurityUtils.getCurrentUserId()))
                .build();
    }

    @GetMapping("/{tableId}/public")
    ApiResult<TableResponse> getPublicTable(@PathVariable String restaurantId, @PathVariable String tableId) {
        return ApiResult.<TableResponse>builder()
                .data(tableService.getPublicTable(restaurantId, tableId))
                .build();
    }

    @GetMapping(value = "/{tableId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    ResponseEntity<byte[]> getQrCode(@PathVariable String restaurantId, @PathVariable String tableId) {
        byte[] qrCode = tableService.generateQrCode(restaurantId, tableId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"table-" + tableId + "-qr.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCode);
    }

    @PutMapping("/{tableId}")
    ApiResult<TableResponse> update(@PathVariable String restaurantId,
                                    @PathVariable String tableId,
                                    @Valid @RequestBody UpdateTableRequest request) {
        return ApiResult.<TableResponse>builder()
                .data(tableService.update(restaurantId, tableId, request, SecurityUtils.getCurrentUserId()))
                .message("Table updated successfully")
                .build();
    }

    @DeleteMapping("/{tableId}")
    ApiResult<Void> delete(@PathVariable String restaurantId, @PathVariable String tableId) {
        tableService.delete(restaurantId, tableId, SecurityUtils.getCurrentUserId());
        return ApiResult.<Void>builder()
                .message("Table deleted successfully")
                .build();
    }
}
