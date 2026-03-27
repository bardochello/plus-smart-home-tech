package ru.yandex.practicum.interaction.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.interaction.dto.OrderDto;
import ru.yandex.practicum.interaction.dto.PaymentDto;

@FeignClient(name = "payment")
public interface PaymentOperations {

    @PostMapping("/api/v1/payment")
    ResponseEntity<PaymentDto> payment(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/totalCost")
    ResponseEntity<Double> getTotalCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/refund")
    ResponseEntity<Void> paymentSuccess(@RequestBody String paymentId);

    @PostMapping("/api/v1/payment/productCost")
    ResponseEntity<Double> productCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/failed")
    ResponseEntity<Void> paymentFailed(@RequestBody String paymentId);
}
