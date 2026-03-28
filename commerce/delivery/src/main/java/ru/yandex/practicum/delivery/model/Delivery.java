package ru.yandex.practicum.delivery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.interaction.dto.enums.DeliveryState;

import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
public class Delivery {
    @Id
    private UUID deliveryId = UUID.randomUUID();

    private UUID orderId;

    @Embedded
    private AddressEmbeddable fromAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "to_country")),
            @AttributeOverride(name = "city", column = @Column(name = "to_city")),
            @AttributeOverride(name = "street", column = @Column(name = "to_street")),
            @AttributeOverride(name = "house", column = @Column(name = "to_house")),
            @AttributeOverride(name = "flat", column = @Column(name = "to_flat"))
    })
    private AddressEmbeddable toAddress;

    @Enumerated(EnumType.STRING)
    private DeliveryState deliveryState = DeliveryState.CREATED;
}
