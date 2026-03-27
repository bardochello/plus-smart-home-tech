package ru.yandex.practicum.interaction.dto;

import lombok.Data;

@Data
public class NewProductInWarehouseRequest {
    private String productId;
    private boolean fragile;
    private DimensionDto dimension;
    private double weight;
}
