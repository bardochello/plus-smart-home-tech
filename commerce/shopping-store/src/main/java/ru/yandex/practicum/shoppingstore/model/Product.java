package ru.yandex.practicum.shoppingstore.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.interaction.dto.enums.ProductCategory;
import ru.yandex.practicum.interaction.dto.enums.ProductState;
import ru.yandex.practicum.interaction.dto.enums.QuantityState;

import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String productId = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageSrc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuantityState quantityState = QuantityState.ENDED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductState productState = ProductState.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory productCategory;

    private double price;
}
