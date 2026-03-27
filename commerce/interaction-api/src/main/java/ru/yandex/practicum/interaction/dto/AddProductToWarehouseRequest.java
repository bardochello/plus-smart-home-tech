package ru.yandex.practicum.interaction.dto;

import lombok.Data;

@Data
public class AddProductToWarehouseRequest {
    private String productId;
    private Long quantity;
}
