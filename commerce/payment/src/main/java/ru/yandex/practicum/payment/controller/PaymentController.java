package ru.yandex.practicum.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.api.PaymentOperations;
import ru.yandex.practicum.interaction.dto.OrderDto;
import ru.yandex.practicum.interaction.dto.PaymentDto;
import ru.yandex.practicum.payment.service.PaymentService;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController implements PaymentOperations {

    private final PaymentService service;

    @Override
    @PostMapping
    public ResponseEntity<PaymentDto> payment(@RequestBody OrderDto order) {
        return ResponseEntity.ok(service.createPayment(order));
    }

    @Override
    @PostMapping("/totalCost")
    public ResponseEntity<Double> getTotalCost(@RequestBody OrderDto order) {
        return ResponseEntity.ok(service.getTotalCost(order));
    }

    @Override
    @PostMapping("/productCost")
    public ResponseEntity<Double> productCost(@RequestBody OrderDto order) {
        return ResponseEntity.ok(service.getProductCost(order));
    }

    @Override
    @PostMapping("/refund")           // success
    public ResponseEntity<Void> paymentSuccess(@RequestBody String paymentId) {
        service.paymentSuccess(paymentId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/failed")
    public ResponseEntity<Void> paymentFailed(@RequestBody String paymentId) {
        service.paymentFailed(paymentId);
        return ResponseEntity.ok().build();
    }
}