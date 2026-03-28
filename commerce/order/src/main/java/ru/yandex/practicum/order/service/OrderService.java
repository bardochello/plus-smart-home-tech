package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.api.*;
import ru.yandex.practicum.interaction.dto.*;
import ru.yandex.practicum.interaction.dto.enums.OrderState;
import ru.yandex.practicum.order.exception.NoOrderFoundException;
import ru.yandex.practicum.order.model.Order;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository repository;
    private final WarehouseOperations warehouseClient;
    private final DeliveryOperations deliveryClient;
    private final PaymentOperations paymentClient;

    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        ShoppingCartDto cart = request.getShoppingCart();
        AddressDto deliveryAddress = request.getDeliveryAddress();

        BookedProductsDto booked = warehouseClient
                .checkProductQuantityEnoughForShoppingCart(cart)
                .getBody();

        Order order = new Order();
        order.setShoppingCartId(cart.getShoppingCartId());
        order.setProducts(cart.getProducts());
        order.setState(OrderState.NEW);
        order.setDeliveryWeight(booked.getDeliveryWeight());
        order.setDeliveryVolume(booked.getDeliveryVolume());
        order.setFragile(booked.isFragile());
        order.setUsername("test-user"); // для тестов

        order = repository.save(order);

        AddressDto warehouseAddr = warehouseClient.getWarehouseAddress().getBody();

        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setOrderId(order.getOrderId());
        deliveryDto.setFromAddress(warehouseAddr);
        deliveryDto.setToAddress(deliveryAddress);
        deliveryDto.setDeliveryState(ru.yandex.practicum.interaction.dto.enums.DeliveryState.CREATED);

        DeliveryDto createdDelivery = deliveryClient.planDelivery(deliveryDto).getBody();

        order.setDeliveryId(createdDelivery.getDeliveryId());
        order = repository.save(order);

        return toDto(order);
    }

    public List<OrderDto> getClientOrders(String username) {
        return repository.findByUsername(username).stream().map(this::toDto).toList();
    }

    public OrderDto assembly(String orderIdStr) {
        Order order = findOrder(orderIdStr);
        warehouseClient.assemblyProductsForOrder(
                new AssemblyProductsForOrderRequest(UUID.fromString(orderIdStr), order.getProducts())
        ).getBody();
        order.setState(OrderState.ASSEMBLED);
        return toDto(repository.save(order));
    }

    public OrderDto payment(String orderIdStr) {
        Order order = findOrder(orderIdStr);

        if (order.getState() == OrderState.ON_PAYMENT) {
            order.setState(OrderState.PAID);
        } else {
            order.setState(OrderState.ON_PAYMENT);
            OrderDto dto = toDto(order);
            PaymentDto paymentDto = paymentClient.payment(dto).getBody();
            order.setPaymentId(paymentDto.getPaymentId());
        }
        return toDto(repository.save(order));
    }

    public OrderDto paymentFailed(String orderIdStr) {
        Order order = findOrder(orderIdStr);
        order.setState(OrderState.PAYMENT_FAILED);
        return toDto(repository.save(order));
    }

    public OrderDto delivery(String orderIdStr) {
        Order order = findOrder(orderIdStr);
        order.setState(OrderState.DELIVERED);
        return toDto(repository.save(order));
    }

    public OrderDto deliveryFailed(String orderIdStr) {
        Order order = findOrder(orderIdStr);
        order.setState(OrderState.DELIVERY_FAILED);
        return toDto(repository.save(order));
    }

    public OrderDto calculateDeliveryCost(String orderIdStr) {
        Order order = findOrder(orderIdStr);
        OrderDto dto = toDto(order);
        Double cost = deliveryClient.deliveryCost(dto).getBody();
        order.setDeliveryPrice(cost);
        return toDto(repository.save(order));
    }

    public OrderDto calculateTotalCost(String orderIdStr) {
        Order order = findOrder(orderIdStr);
        OrderDto dto = toDto(order);
        Double total = paymentClient.getTotalCost(dto).getBody();
        order.setTotalPrice(total);
        return toDto(repository.save(order));
    }

    public OrderDto productReturn(ProductReturnRequest request) {
        Order order = findOrder(request.getOrderId().toString());
        order.setState(OrderState.PRODUCT_RETURNED);
        return toDto(repository.save(order));
    }

    public OrderDto assemblyFailed(String orderIdStr) {
        Order order = findOrder(orderIdStr);
        order.setState(OrderState.ASSEMBLY_FAILED);
        return toDto(repository.save(order));
    }

    public OrderDto complete(String orderIdStr) {
        Order order = findOrder(orderIdStr);
        order.setState(OrderState.COMPLETED);
        return toDto(repository.save(order));
    }

    private Order findOrder(String orderIdStr) {
        UUID id = UUID.fromString(orderIdStr);
        return repository.findById(id)
                .orElseThrow(() -> new NoOrderFoundException("Order not found: " + orderIdStr));
    }

    private OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setShoppingCartId(order.getShoppingCartId());
        dto.setProducts(order.getProducts());
        dto.setPaymentId(order.getPaymentId());
        dto.setDeliveryId(order.getDeliveryId());
        dto.setState(order.getState());
        dto.setDeliveryWeight(order.getDeliveryWeight());
        dto.setDeliveryVolume(order.getDeliveryVolume());
        dto.setFragile(order.getFragile());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setDeliveryPrice(order.getDeliveryPrice());
        dto.setProductPrice(order.getProductPrice());
        return dto;
    }
}
