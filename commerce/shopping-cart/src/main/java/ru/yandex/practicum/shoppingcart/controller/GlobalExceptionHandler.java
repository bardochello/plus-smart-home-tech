package ru.yandex.practicum.shoppingcart.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.shoppingcart.exception.InsufficientStockException;
import ru.yandex.practicum.shoppingcart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.shoppingcart.exception.NotAuthorizedUserException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotAuthorizedUserException.class)
    public ResponseEntity<String> handleNotAuthorized(NotAuthorizedUserException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    public ResponseEntity<String> handleNoProducts(NoProductsInShoppingCartException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<String> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}