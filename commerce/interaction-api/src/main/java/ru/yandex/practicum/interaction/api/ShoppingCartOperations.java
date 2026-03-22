package ru.yandex.practicum.interaction.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.interaction.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "shopping-cart")
public interface ShoppingCartOperations {

    @GetMapping("/api/v1/shopping-cart")
    ResponseEntity<ShoppingCartDto> getShoppingCart(@RequestParam("username") String username);

    @PutMapping("/api/v1/shopping-cart")
    ResponseEntity<ShoppingCartDto> addProductToShoppingCart(
            @RequestParam("username") String username,
            @RequestBody Map<String, Long> products);

    @DeleteMapping("/api/v1/shopping-cart")
    ResponseEntity<Void> deactivateCurrentShoppingCart(@RequestParam("username") String username);

    @PostMapping("/api/v1/shopping-cart/remove")
    ResponseEntity<ShoppingCartDto> removeFromShoppingCart(
            @RequestParam("username") String username,
            @RequestBody List<String> productIds);

    @PostMapping("/api/v1/shopping-cart/change-quantity")
    ResponseEntity<ShoppingCartDto> changeProductQuantity(
            @RequestParam("username") String username,
            @RequestBody ChangeProductQuantityRequest request);
}
