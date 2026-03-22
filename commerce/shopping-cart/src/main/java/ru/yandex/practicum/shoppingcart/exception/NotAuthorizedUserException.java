package ru.yandex.practicum.shoppingcart.exception;

public class NotAuthorizedUserException extends RuntimeException {
    public NotAuthorizedUserException() {
        super("Имя пользователя не должно быть пустым");
    }
}
