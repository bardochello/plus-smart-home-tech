package ru.yandex.practicum.interaction.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.dto.*;
import ru.yandex.practicum.interaction.dto.enums.ProductCategory;

import java.util.List;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreOperations {

    @GetMapping("/api/v1/shopping-store")
    ResponseEntity<Page<ProductDto>> getProducts(
            @RequestParam("category") ProductCategory category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", required = false) List<String> sort);

    @PutMapping("/api/v1/shopping-store")
    ResponseEntity<ProductDto> createNewProduct(@RequestBody ProductDto product);

    @PostMapping("/api/v1/shopping-store")
    ResponseEntity<ProductDto> updateProduct(@RequestBody ProductDto product);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    ResponseEntity<Boolean> removeProductFromStore(@RequestBody String productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    ResponseEntity<Boolean> setProductQuantityState(@RequestBody SetProductQuantityStateRequest request);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ResponseEntity<ProductDto> getProduct(@PathVariable("productId") String productId);
}