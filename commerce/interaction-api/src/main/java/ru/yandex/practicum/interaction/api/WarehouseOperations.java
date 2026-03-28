package ru.yandex.practicum.interaction.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.dto.*;

import java.util.Map;

@FeignClient(name = "warehouse")
public interface WarehouseOperations {

    @PutMapping("/api/v1/warehouse")
    ResponseEntity<Void> newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/api/v1/warehouse/check")
    ResponseEntity<BookedProductsDto> checkProductQuantityEnoughForShoppingCart(
            @RequestBody ShoppingCartDto shoppingCart);

    @PostMapping("/api/v1/warehouse/add")
    ResponseEntity<Void> addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request);

    @GetMapping("/api/v1/warehouse/address")
    ResponseEntity<AddressDto> getWarehouseAddress();

    @PostMapping("/api/v1/warehouse/assembly")
    ResponseEntity<BookedProductsDto> assemblyProductsForOrder(@RequestBody AssemblyProductsForOrderRequest request);

    @PostMapping("/api/v1/warehouse/shipped")
    ResponseEntity<Void> shippedToDelivery(@RequestBody ShippedToDeliveryRequest request);

    @PostMapping("/api/v1/warehouse/return")
    ResponseEntity<Void> acceptReturn(@RequestBody Map<String, Long> products);
}
