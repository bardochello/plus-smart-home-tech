package ru.yandex.practicum.interaction.dto;

import lombok.Data;
import ru.yandex.practicum.interaction.dto.enums.OrderState;

import java.util.Map;
import java.util.UUID;

@Data
public class OrderDto {
    private UUID orderId;
    private UUID shoppingCartId;
    private Map<String, Long> products;
    private UUID paymentId;
    private UUID deliveryId;
    private OrderState state;
    private Double deliveryWeight;
    private Double deliveryVolume;
    private Boolean fragile;
    private Double totalPrice;
    private Double deliveryPrice;
    private Double productPrice;
}
