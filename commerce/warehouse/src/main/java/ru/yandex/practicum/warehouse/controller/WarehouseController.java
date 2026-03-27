package ru.yandex.practicum.warehouse.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.api.WarehouseOperations;
import ru.yandex.practicum.interaction.dto.*;
import ru.yandex.practicum.warehouse.service.WarehouseService;

import java.util.Map;

@Slf4j
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
        log.debug("Проверка достаточного количества товаров для корзины {}", cart.getShoppingCartId());
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

    @Override
    @PostMapping("/assembly")
    public ResponseEntity<BookedProductsDto> assemblyProductsForOrder(@RequestBody AssemblyProductsForOrderRequest request) {
        return ResponseEntity.ok(service.assemblyProductsForOrder(request));
    }

    @Override
    @PostMapping("/shipped")
    public ResponseEntity<Void> shippedToDelivery(@RequestBody ShippedToDeliveryRequest request) {
        service.shippedToDelivery(request);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/return")
    public ResponseEntity<Void> acceptReturn(@RequestBody Map<String, Long> products) {
        service.acceptReturn(products);
        return ResponseEntity.ok().build();
    }
}