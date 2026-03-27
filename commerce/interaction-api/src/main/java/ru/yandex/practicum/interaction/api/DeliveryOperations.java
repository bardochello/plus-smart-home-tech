package ru.yandex.practicum.interaction.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.dto.DeliveryDto;
import ru.yandex.practicum.interaction.dto.OrderDto;

@FeignClient(name = "delivery")
public interface DeliveryOperations {

    @PutMapping("/api/v1/delivery")
    ResponseEntity<DeliveryDto> planDelivery(@RequestBody DeliveryDto delivery);

    @PostMapping("/api/v1/delivery/cost")
    ResponseEntity<Double> deliveryCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/delivery/picked")
    ResponseEntity<Void> deliveryPicked(@RequestBody String orderId);

    @PostMapping("/api/v1/delivery/successful")
    ResponseEntity<Void> deliverySuccessful(@RequestBody String orderId);

    @PostMapping("/api/v1/delivery/failed")
    ResponseEntity<Void> deliveryFailed(@RequestBody String orderId);
}
