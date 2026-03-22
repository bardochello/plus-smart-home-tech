package ru.yandex.practicum.shoppingstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.api.ShoppingStoreOperations;
import ru.yandex.practicum.interaction.dto.*;
import ru.yandex.practicum.interaction.dto.enums.ProductCategory;
import ru.yandex.practicum.shoppingstore.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ShoppingStoreController implements ShoppingStoreOperations {

    private final ProductService service;

    @Override
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getProducts(
            @RequestParam("category") ProductCategory category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", required = false) List<String> sort) {
        return ResponseEntity.ok(service.getProducts(category, page, size, sort));
    }

    @Override
    @PutMapping
    public ResponseEntity<ProductDto> createNewProduct(@RequestBody ProductDto product) {
        return ResponseEntity.ok(service.createNewProduct(product));
    }

    @Override
    @PostMapping
    public ResponseEntity<ProductDto> updateProduct(@RequestBody ProductDto product) {
        return ResponseEntity.ok(service.updateProduct(product));
    }

    @Override
    @PostMapping("/removeProductFromStore")
    public ResponseEntity<Boolean> removeProductFromStore(@RequestBody String productId) {
        return ResponseEntity.ok(service.removeProductFromStore(productId));
    }

    @Override
    @PostMapping("/quantityState")
    public ResponseEntity<Boolean> setProductQuantityState(@RequestBody SetProductQuantityStateRequest request) {
        return ResponseEntity.ok(service.setProductQuantityState(request));
    }

    @Override
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable("productId") String productId) {
        return ResponseEntity.ok(service.getProduct(productId));
    }
}