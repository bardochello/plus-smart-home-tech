package ru.yandex.practicum.shoppingcart.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts")
@Getter
@Setter
@NoArgsConstructor
public class ShoppingCart {

    @Id
    private UUID shoppingCartId = UUID.randomUUID();

    @Column(nullable = false)
    private String username;

    @Column(name = "activated", nullable = false)
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "carts_products",
            joinColumns = @JoinColumn(name = "shopping_cart_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<String, Long> products = new HashMap<>();
}