package ru.yandex.practicum.warehouse.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.warehouse.exception.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    public ResponseEntity<String> handleAlreadyExists(SpecifiedProductAlreadyInWarehouseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    public ResponseEntity<String> handleNotFound(NoSpecifiedProductInWarehouseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouse.class)
    public ResponseEntity<String> handleLowQuantity(ProductInShoppingCartLowQuantityInWarehouse ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
