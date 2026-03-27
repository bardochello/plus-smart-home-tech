package ru.yandex.practicum.order.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.interaction.dto.enums.OrderState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {
    @Id
    private UUID orderId = UUID.randomUUID();

    private UUID shoppingCartId;
    private String username;

    @ElementCollection
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<String, Long> products = new HashMap<>();

    private UUID paymentId;
    private UUID deliveryId;

    @Enumerated(EnumType.STRING)
    private OrderState state = OrderState.NEW;

    private Double deliveryWeight;
    private Double deliveryVolume;
    private Boolean fragile;
    private Double totalPrice;
    private Double deliveryPrice;
    private Double productPrice;
}
