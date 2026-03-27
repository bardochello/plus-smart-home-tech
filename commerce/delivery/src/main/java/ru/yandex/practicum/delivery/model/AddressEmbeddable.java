package ru.yandex.practicum.delivery.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class AddressEmbeddable {
    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;
}
