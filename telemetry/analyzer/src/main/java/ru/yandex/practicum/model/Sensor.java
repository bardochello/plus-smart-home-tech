package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sensors")
@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class Sensor {
    @Id
    private String id;
    @Column(name = "hub_id")
    private String hubId;
}
