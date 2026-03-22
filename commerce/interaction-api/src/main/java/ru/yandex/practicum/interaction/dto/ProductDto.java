package ru.yandex.practicum.interaction.dto;

import lombok.Data;
import ru.yandex.practicum.interaction.dto.enums.*;

@Data
public class ProductDto {
    private String productId;
    private String productName;
    private String description;
    private String imageSrc;
    private QuantityState quantityState;
    private ProductState productState;
    private ProductCategory productCategory;
    private double price;
}
