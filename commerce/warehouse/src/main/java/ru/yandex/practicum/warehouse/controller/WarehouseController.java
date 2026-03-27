package ru.yandex.practicum.warehouse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.api.WarehouseOperations;
import ru.yandex.practicum.interaction.dto.*;
import ru.yandex.practicum.warehouse.service.WarehouseService;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseOperations {

    private final WarehouseService service;

    @Override
    @PutMapping
    public ResponseEntity<Void> newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request) {
        service.newProductInWarehouse(request);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/check")
    public ResponseEntity<BookedProductsDto> checkProductQuantityEnoughForShoppingCart(
            @RequestBody ShoppingCartDto cart) {
        return ResponseEntity.ok(service.checkProductQuantityEnoughForShoppingCart(cart));
    }

    @Override
    @PostMapping("/add")
    public ResponseEntity<Void> addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request) {
        service.addProductToWarehouse(request);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/address")
    public ResponseEntity<AddressDto> getWarehouseAddress() {
        return ResponseEntity.ok(service.getWarehouseAddress());
    }
}