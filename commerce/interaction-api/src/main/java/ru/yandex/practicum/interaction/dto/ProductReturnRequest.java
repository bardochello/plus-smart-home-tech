package ru.yandex.practicum.interaction.dto;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class ProductReturnRequest {
    private UUID orderId;
    private Map<String, Long> products;
}
