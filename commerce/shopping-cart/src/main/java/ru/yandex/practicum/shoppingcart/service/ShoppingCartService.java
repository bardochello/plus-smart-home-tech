package ru.yandex.practicum.shoppingcart.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.api.WarehouseOperations;
import ru.yandex.practicum.interaction.dto.ShoppingCartDto;
import ru.yandex.practicum.shoppingcart.exception.InsufficientStockException;
import ru.yandex.practicum.shoppingcart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.shoppingcart.exception.NotAuthorizedUserException;
import ru.yandex.practicum.shoppingcart.model.ShoppingCart;
import ru.yandex.practicum.shoppingcart.repository.ShoppingCartRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ShoppingCartService {

    private final ShoppingCartRepository repository;
    private final WarehouseOperations warehouseClient;

    public ShoppingCartDto getShoppingCart(String username) {
        validateUsername(username);
        ShoppingCart cart = getOrCreateCart(username);
        return toDto(cart);
    }

    public ShoppingCartDto addProduct(String username, Map<String, Long> newProducts) {
        validateUsername(username);

        ShoppingCart cart = getOrCreateCart(username);
        if (!cart.isActive()) {
            throw new IllegalStateException("Корзина деактивирована");
        }

        Map<String, Long> tempProducts = new HashMap<>(cart.getProducts());
        newProducts.forEach((pid, qty) -> tempProducts.merge(pid, qty, Long::sum));

        ShoppingCartDto tempCart = new ShoppingCartDto(cart.getShoppingCartId(), tempProducts);

        checkWarehouseStock(tempCart);

        newProducts.forEach((productId, qty) ->
                cart.getProducts().merge(productId, qty, Long::sum));

        return toDto(repository.save(cart));
    }

    public void deactivate(String username) {
        validateUsername(username);
        ShoppingCart cart = getOrCreateCart(username);
        cart.setActive(false);
        repository.save(cart);
    }

    public ShoppingCartDto removeFromShoppingCart(String username, List<String> productIds) {
        validateUsername(username);
        ShoppingCart cart = getOrCreateCart(username);

        if (productIds.stream().anyMatch(id -> !cart.getProducts().containsKey(id))) {
            throw new NoProductsInShoppingCartException();
        }

        productIds.forEach(cart.getProducts()::remove);
        return toDto(repository.save(cart));
    }

    public ShoppingCartDto changeProductQuantity(String username, String productId, Long newQuantity) {
        validateUsername(username);

        ShoppingCart cart = getOrCreateCart(username);
        if (!cart.isActive()) {
            throw new IllegalStateException("Корзина деактивирована");
        }

        if (!cart.getProducts().containsKey(productId)) {
            throw new NoProductsInShoppingCartException();
        }

        Map<String, Long> tempProducts = new HashMap<>(cart.getProducts());
        if (newQuantity <= 0) {
            tempProducts.remove(productId);
        } else {
            tempProducts.put(productId, newQuantity);
        }

        ShoppingCartDto tempCart = new ShoppingCartDto(cart.getShoppingCartId(), tempProducts);

        checkWarehouseStock(tempCart);

        if (newQuantity <= 0) {
            cart.getProducts().remove(productId);
        } else {
            cart.getProducts().put(productId, newQuantity);
        }

        return toDto(repository.save(cart));
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException();
        }
    }

    private void checkWarehouseStock(ShoppingCartDto tempCart) {
        try {
            warehouseClient.checkProductQuantityEnoughForShoppingCart(tempCart);
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new InsufficientStockException("Недостаточно товаров на складе: " + e.contentUTF8());
            }
            throw e;
        }
    }

    private ShoppingCart getOrCreateCart(String username) {
        return repository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> {
                    ShoppingCart cart = new ShoppingCart();
                    cart.setUsername(username);
                    return repository.save(cart);
                });
    }

    private ShoppingCartDto toDto(ShoppingCart cart) {
        return new ShoppingCartDto(cart.getShoppingCartId(), new HashMap<>(cart.getProducts()));
    }
}