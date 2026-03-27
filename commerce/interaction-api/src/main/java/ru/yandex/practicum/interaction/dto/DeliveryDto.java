package ru.yandex.practicum.interaction.dto;

import lombok.Data;
import ru.yandex.practicum.interaction.dto.enums.DeliveryState;

import java.util.UUID;

@Data
public class DeliveryDto {

    private UUID deliveryId;

    private AddressDto fromAddress;

    private AddressDto toAddress;

    private UUID orderId;

    private DeliveryState deliveryState;
}
