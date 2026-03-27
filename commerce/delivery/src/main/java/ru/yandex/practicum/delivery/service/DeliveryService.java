package ru.yandex.practicum.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.delivery.exception.NoDeliveryFoundException;
import ru.yandex.practicum.delivery.model.AddressEmbeddable;
import ru.yandex.practicum.delivery.model.Delivery;
import ru.yandex.practicum.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.interaction.api.OrderOperations;
import ru.yandex.practicum.interaction.api.WarehouseOperations;
import ru.yandex.practicum.interaction.dto.*;
import ru.yandex.practicum.interaction.dto.enums.DeliveryState;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

    private final DeliveryRepository repository;
    private final WarehouseOperations warehouseClient;
    private final OrderOperations orderClient;

    public DeliveryDto planDelivery(DeliveryDto dto) {
        Delivery delivery = new Delivery();
        delivery.setOrderId(dto.getOrderId());
        delivery.setFromAddress(convert(dto.getFromAddress()));
        delivery.setToAddress(convert(dto.getToAddress()));
        delivery.setDeliveryState(DeliveryState.CREATED);

        delivery = repository.save(delivery);

        dto.setDeliveryId(delivery.getDeliveryId());
        dto.setDeliveryState(delivery.getDeliveryState());
        return dto;
    }

    public double deliveryCost(OrderDto order) {
        AddressDto warehouseAddr = warehouseClient.getWarehouseAddress().getBody();

        double base = 5.0;

        String warehouseCountry = warehouseAddr.getCountry();
        if (warehouseCountry != null && warehouseCountry.contains("ADDRESS_2")) {
            base = base * 2 + base;   // 15
        } else {
            base = base * 1 + base;   // 10
        }

        if (Boolean.TRUE.equals(order.getFragile())) {
            base += base * 0.2;
        }

        base += (order.getDeliveryWeight() != null ? order.getDeliveryWeight() : 0.0) * 0.3;
        base += (order.getDeliveryVolume() != null ? order.getDeliveryVolume() : 0.0) * 0.2;

        if (order.getDeliveryId() != null) {
            Delivery delivery = repository.findById(order.getDeliveryId())
                    .orElseThrow(() -> new NoDeliveryFoundException("Delivery not found: " + order.getDeliveryId()));

            AddressDto toAddress = convertFromEmbeddable(delivery.getToAddress());
            if (!isSameStreet(warehouseAddr, toAddress)) {
                base += base * 0.2;
            }
        } else {
            base += base * 0.2;
        }

        return Math.round(base * 100.0) / 100.0;
    }


    public void deliveryPicked(String orderId) {
        UUID orderUuid = UUID.fromString(orderId);

        Delivery delivery = repository.findByOrderId(orderUuid)
                .orElseThrow(() -> new NoDeliveryFoundException("No delivery found for order: " + orderId));

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        repository.save(delivery);

        ShippedToDeliveryRequest shippedRequest = new ShippedToDeliveryRequest();
        shippedRequest.setOrderId(orderUuid);
        shippedRequest.setDeliveryId(delivery.getDeliveryId());
        warehouseClient.shippedToDelivery(shippedRequest);

        orderClient.assembly(orderId);
    }

    public void deliverySuccessful(String orderId) {
        updateDeliveryState(orderId, DeliveryState.DELIVERED);
        orderClient.delivery(orderId);
    }

    public void deliveryFailed(String orderId) {
        updateDeliveryState(orderId, DeliveryState.FAILED);
        orderClient.deliveryFailed(orderId);
    }

    private void updateDeliveryState(String orderIdStr, DeliveryState newState) {
        UUID orderUuid = UUID.fromString(orderIdStr);

        Delivery delivery = repository.findByOrderId(orderUuid)
                .orElseThrow(() -> new NoDeliveryFoundException("No delivery found for order: " + orderIdStr));

        delivery.setDeliveryState(newState);
        repository.save(delivery);
    }

    private boolean isSameStreet(AddressDto wh, AddressDto to) {
        if (wh.getStreet() == null || to.getStreet() == null) {
            return false;
        }
        return wh.getStreet().equalsIgnoreCase(to.getStreet());
    }

    private AddressEmbeddable convert(AddressDto dto) {
        AddressEmbeddable embed = new AddressEmbeddable();
        embed.setCountry(dto.getCountry());
        embed.setCity(dto.getCity());
        embed.setStreet(dto.getStreet());
        embed.setHouse(dto.getHouse());
        embed.setFlat(dto.getFlat());
        return embed;
    }

    private AddressDto convertFromEmbeddable(AddressEmbeddable embed) {
        AddressDto dto = new AddressDto();
        dto.setCountry(embed.getCountry());
        dto.setCity(embed.getCity());
        dto.setStreet(embed.getStreet());
        dto.setHouse(embed.getHouse());
        dto.setFlat(embed.getFlat());
        return dto;
    }
}
