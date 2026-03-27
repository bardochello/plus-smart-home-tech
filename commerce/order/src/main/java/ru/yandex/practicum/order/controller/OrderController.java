package ru.yandex.practicum.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.api.OrderOperations;
import ru.yandex.practicum.interaction.dto.*;
import ru.yandex.practicum.order.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController implements OrderOperations {

    private final OrderService service;

    @Override
    @PutMapping
    public ResponseEntity<OrderDto> createNewOrder(@RequestBody CreateNewOrderRequest request) {
        return ResponseEntity.ok(service.createNewOrder(request));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<OrderDto>> getClientOrders(@RequestParam("username") String username) {
        return ResponseEntity.ok(service.getClientOrders(username));
    }

    @Override
    @PostMapping("/assembly")
    public ResponseEntity<OrderDto> assembly(@RequestBody String orderId) {
        return ResponseEntity.ok(service.assembly(orderId));
    }

    @Override
    @PostMapping("/assembly/failed")
    public ResponseEntity<OrderDto> assemblyFailed(@RequestBody String orderId) {
        return ResponseEntity.ok(service.assemblyFailed(orderId));
    }

    @Override
    @PostMapping("/payment")
    public ResponseEntity<OrderDto> payment(@RequestBody String orderId) {
        return ResponseEntity.ok(service.payment(orderId));
    }

    @Override
    @PostMapping("/payment/failed")
    public ResponseEntity<OrderDto> paymentFailed(@RequestBody String orderId) {
        return ResponseEntity.ok(service.paymentFailed(orderId));
    }

    @Override
    @PostMapping("/delivery")
    public ResponseEntity<OrderDto> delivery(@RequestBody String orderId) {
        return ResponseEntity.ok(service.delivery(orderId));
    }

    @Override
    @PostMapping("/delivery/failed")
    public ResponseEntity<OrderDto> deliveryFailed(@RequestBody String orderId) {
        return ResponseEntity.ok(service.deliveryFailed(orderId));
    }

    @Override
    @PostMapping("/completed")
    public ResponseEntity<OrderDto> complete(@RequestBody String orderId) {
        return ResponseEntity.ok(service.complete(orderId));
    }

    @Override
    @PostMapping("/calculate/total")
    public ResponseEntity<OrderDto> calculateTotalCost(@RequestBody String orderId) {
        return ResponseEntity.ok(service.calculateTotalCost(orderId));
    }

    @Override
    @PostMapping("/calculate/delivery")
    public ResponseEntity<OrderDto> calculateDeliveryCost(@RequestBody String orderId) {
        return ResponseEntity.ok(service.calculateDeliveryCost(orderId));
    }

    @Override
    @PostMapping("/return")
    public ResponseEntity<OrderDto> productReturn(@RequestBody ProductReturnRequest request) {
        return ResponseEntity.ok(service.productReturn(request));
    }
}
