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
    @Column(name = "product_id")
    private String productId = UUID.randomUUID().toString();

    @Column(name = "name")
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_src")
    private String imageSrc;

    @Column(name = "quantity_state")
    @Enumerated(EnumType.STRING)
    private QuantityState quantityState = QuantityState.ENDED;

    @Column(name = "product_state")
    @Enumerated(EnumType.STRING)
    private ProductState productState = ProductState.ACTIVE;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private ProductCategory productCategory;

    @Column(name = "price")
    private double price;

    @Column(name = "rating")
    private Float rating;
}
