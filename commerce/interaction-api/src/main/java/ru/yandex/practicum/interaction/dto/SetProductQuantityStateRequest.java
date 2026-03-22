package ru.yandex.practicum.interaction.dto;

import lombok.Data;
import ru.yandex.practicum.interaction.dto.enums.QuantityState;

@Data
public class SetProductQuantityStateRequest {
    private String productId;
    private QuantityState quantityState;
}
