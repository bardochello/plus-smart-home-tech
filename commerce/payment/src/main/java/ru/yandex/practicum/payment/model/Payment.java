package ru.yandex.practicum.payment.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.interaction.dto.enums.PaymentStatus;

import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    private UUID paymentId = UUID.randomUUID();

    private UUID orderId;

    private Double productPrice;
    private Double deliveryPrice;
    private Double fee;
    private Double total;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;
}
