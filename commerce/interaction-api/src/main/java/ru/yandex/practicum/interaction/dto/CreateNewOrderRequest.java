package ru.yandex.practicum.interaction.dto;

import lombok.Data;

@Data
public class CreateNewOrderRequest {
    private ShoppingCartDto shoppingCart;
    private AddressDto deliveryAddress;
}
