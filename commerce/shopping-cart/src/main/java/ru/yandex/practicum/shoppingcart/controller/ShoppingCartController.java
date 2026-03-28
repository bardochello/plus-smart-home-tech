package ru.yandex.practicum.shoppingcart.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.api.ShoppingCartOperations;
import ru.yandex.practicum.interaction.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.interaction.dto.ShoppingCartDto;
import ru.yandex.practicum.shoppingcart.service.ShoppingCartService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController implements ShoppingCartOperations {

    private final ShoppingCartService service;

    @Override
    @GetMapping
    public ResponseEntity<ShoppingCartDto> getShoppingCart(@RequestParam("username") String username) {
        log.debug("Получен запрос на получение списка товаров в корзине пользователя {}", username);
        return ResponseEntity.ok(service.getShoppingCart(username));
    }

    @Override
    @PutMapping
    public ResponseEntity<ShoppingCartDto> addProductToShoppingCart(
            @RequestParam("username") String username,
            @RequestBody Map<String, Long> products) {
        return ResponseEntity.ok(service.addProduct(username, products));
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deactivateCurrentShoppingCart(@RequestParam("username") String username) {
        service.deactivate(username);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/remove")
    public ResponseEntity<ShoppingCartDto> removeFromShoppingCart(
            @RequestParam("username") String username,
            @RequestBody List<String> productIds) {
        return ResponseEntity.ok(service.removeFromShoppingCart(username, productIds));
    }

    @Override
    @PostMapping("/change-quantity")
    public ResponseEntity<ShoppingCartDto> changeProductQuantity(
            @RequestParam("username") String username,
            @RequestBody ChangeProductQuantityRequest request) {
        return ResponseEntity.ok(service.changeProductQuantity(
                username, request.getProductId(), request.getNewQuantity()));
    }
}