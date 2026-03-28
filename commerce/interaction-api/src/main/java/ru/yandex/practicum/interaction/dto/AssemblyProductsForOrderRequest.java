package ru.yandex.practicum.interaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssemblyProductsForOrderRequest {

    private UUID orderId;
    private Map<String, Long> products;
}