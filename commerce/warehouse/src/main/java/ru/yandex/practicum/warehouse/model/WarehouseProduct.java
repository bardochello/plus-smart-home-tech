package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouse_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseProduct {

    @Id
    private String productId;  // UUID как строка

    @Embedded
    private DimensionEmbeddable dimension;

    private double weight;

    private boolean fragile;

    private long quantity = 0;
}
