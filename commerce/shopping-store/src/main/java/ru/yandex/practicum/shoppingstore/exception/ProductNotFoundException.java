package ru.yandex.practicum.shoppingstore.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException() {
        super("Товар по идентификатору в БД не найден");
    }
}
