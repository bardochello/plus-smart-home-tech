package ru.yandex.practicum.warehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SpecifiedProductAlreadyInWarehouseException extends RuntimeException {
    public SpecifiedProductAlreadyInWarehouseException(String productId) {
        super("Товар с id " + productId + " уже зарегистрирован на складе");
    }
}
