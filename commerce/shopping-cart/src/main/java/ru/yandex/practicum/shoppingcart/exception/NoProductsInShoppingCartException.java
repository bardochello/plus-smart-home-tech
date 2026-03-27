package ru.yandex.practicum.shoppingcart.exception;

public class NoProductsInShoppingCartException extends RuntimeException {
    public NoProductsInShoppingCartException() {
        super("Нет искомых товаров в корзине");
    }
}