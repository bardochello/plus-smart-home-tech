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
    @Column(name = "product_id")
    private String productId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "width", column = @Column(name = "dimension_width")),
            @AttributeOverride(name = "height", column = @Column(name = "dimension_height")),
            @AttributeOverride(name = "depth", column = @Column(name = "dimension_depth"))
    })
    private DimensionEmbeddable dimension;

    private double weight;
    private boolean fragile;
    private long quantity = 0;
}
