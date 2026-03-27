package ru.yandex.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.api.OrderOperations;
import ru.yandex.practicum.interaction.api.ShoppingStoreOperations;
import ru.yandex.practicum.interaction.dto.OrderDto;
import ru.yandex.practicum.interaction.dto.PaymentDto;
import ru.yandex.practicum.interaction.dto.ProductDto;
import ru.yandex.practicum.interaction.dto.enums.PaymentStatus;
import ru.yandex.practicum.payment.exception.NoOrderFoundException;
import ru.yandex.practicum.payment.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.payment.model.Payment;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository repository;
    private final ShoppingStoreOperations storeClient;
    private final OrderOperations orderClient;

    public double getProductCost(OrderDto order) {
        if (order.getProducts() == null || order.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("Нет товаров в заказе");
        }

        double sum = 0.0;
        for (var entry : order.getProducts().entrySet()) {
            ProductDto product = storeClient.getProduct(entry.getKey()).getBody();
            if (product == null) {
                throw new NotEnoughInfoInOrderToCalculateException("Товар не найден: " + entry.getKey());
            }
            sum += product.getPrice() * entry.getValue();
        }
        return sum;
    }

    public double getTotalCost(OrderDto order) {
        double productCost = order.getProductPrice() != null
                ? order.getProductPrice()
                : getProductCost(order);

        double deliveryCost = order.getDeliveryPrice() != null ? order.getDeliveryPrice() : 0.0;
        double fee = productCost * 0.1;

        return productCost + deliveryCost + fee;
    }

    public PaymentDto createPayment(OrderDto order) {
        double productCost = getProductCost(order);
        double deliveryPrice = order.getDeliveryPrice() != null ? order.getDeliveryPrice() : 0.0;
        double fee = productCost * 0.1;
        double total = productCost + deliveryPrice + fee;

        Payment payment = new Payment();
        payment.setOrderId(order.getOrderId());
        payment.setProductPrice(productCost);
        payment.setDeliveryPrice(deliveryPrice);
        payment.setFee(fee);
        payment.setTotal(total);
        payment.setStatus(PaymentStatus.PENDING);

        payment = repository.save(payment);

        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(payment.getPaymentId());
        dto.setTotalPayment(total);
        dto.setDeliveryTotal(deliveryPrice);
        dto.setFeeTotal(fee);

        return dto;
    }

    public void paymentSuccess(String paymentIdStr) {
        UUID paymentId = UUID.fromString(paymentIdStr);
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Payment not found: " + paymentIdStr));

        payment.setStatus(PaymentStatus.SUCCESS);
        repository.save(payment);

        orderClient.payment(payment.getOrderId().toString());
    }

    public void paymentFailed(String paymentIdStr) {
        UUID paymentId = UUID.fromString(paymentIdStr);
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Payment not found: " + paymentIdStr));

        payment.setStatus(PaymentStatus.FAILED);
        repository.save(payment);

        orderClient.paymentFailed(payment.getOrderId().toString());
    }
}
