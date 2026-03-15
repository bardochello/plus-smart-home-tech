package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "scenario_conditions")
@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@IdClass(ScenarioConditionId.class)
public class ScenarioCondition {
    @Id
    @ManyToOne
    @JoinColumn(name = "scenario_id")
    @ToString.Exclude
    private Scenario scenario;

    @Id
    @ManyToOne
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @Id
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "condition_id")
    private Condition condition;
}

@Data
@NoArgsConstructor @AllArgsConstructor
class ScenarioConditionId implements Serializable {
    private Long scenario;
    private String sensor;
    private Long condition;
}
