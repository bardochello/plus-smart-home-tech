package ru.yandex.practicum.delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.delivery.service.DeliveryService;
import ru.yandex.practicum.interaction.api.DeliveryOperations;
import ru.yandex.practicum.interaction.dto.DeliveryDto;
import ru.yandex.practicum.interaction.dto.OrderDto;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController implements DeliveryOperations {

    private final DeliveryService service;

    @Override
    @PutMapping
    public ResponseEntity<DeliveryDto> planDelivery(@RequestBody DeliveryDto delivery) {
        return ResponseEntity.ok(service.planDelivery(delivery));
    }

    @Override
    @PostMapping("/cost")
    public ResponseEntity<Double> deliveryCost(@RequestBody OrderDto order) {
        return ResponseEntity.ok(service.deliveryCost(order));
    }

    @Override
    @PostMapping("/picked")
    public ResponseEntity<Void> deliveryPicked(@RequestBody String orderId) {
        service.deliveryPicked(orderId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/successful")
    public ResponseEntity<Void> deliverySuccessful(@RequestBody String orderId) {
        service.deliverySuccessful(orderId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/failed")
    public ResponseEntity<Void> deliveryFailed(@RequestBody String orderId) {
        service.deliveryFailed(orderId);
        return ResponseEntity.ok().build();
    }
}
