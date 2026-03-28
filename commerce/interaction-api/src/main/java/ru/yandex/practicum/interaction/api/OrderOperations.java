package ru.yandex.practicum.interaction.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interaction.dto.CreateNewOrderRequest;
import ru.yandex.practicum.interaction.dto.OrderDto;
import ru.yandex.practicum.interaction.dto.ProductReturnRequest;

import java.util.List;

@FeignClient(name = "order")
public interface OrderOperations {

    @PutMapping("/api/v1/order")
    ResponseEntity<OrderDto> createNewOrder(@RequestBody CreateNewOrderRequest request);

    @GetMapping("/api/v1/order")
    ResponseEntity<List<OrderDto>> getClientOrders(@RequestParam("username") String username);

    @PostMapping("/api/v1/order/assembly")
    ResponseEntity<OrderDto> assembly(@RequestBody String orderId);

    @PostMapping("/api/v1/order/assembly/failed")
    ResponseEntity<OrderDto> assemblyFailed(@RequestBody String orderId);

    @PostMapping("/api/v1/order/payment")
    ResponseEntity<OrderDto> payment(@RequestBody String orderId);

    @PostMapping("/api/v1/order/payment/failed")
    ResponseEntity<OrderDto> paymentFailed(@RequestBody String orderId);

    @PostMapping("/api/v1/order/delivery")
    ResponseEntity<OrderDto> delivery(@RequestBody String orderId);

    @PostMapping("/api/v1/order/delivery/failed")
    ResponseEntity<OrderDto> deliveryFailed(@RequestBody String orderId);

    @PostMapping("/api/v1/order/completed")
    ResponseEntity<OrderDto> complete(@RequestBody String orderId);

    @PostMapping("/api/v1/order/calculate/total")
    ResponseEntity<OrderDto> calculateTotalCost(@RequestBody String orderId);

    @PostMapping("/api/v1/order/calculate/delivery")
    ResponseEntity<OrderDto> calculateDeliveryCost(@RequestBody String orderId);

    @PostMapping("/api/v1/order/return")
    ResponseEntity<OrderDto> productReturn(@RequestBody ProductReturnRequest request);
}
