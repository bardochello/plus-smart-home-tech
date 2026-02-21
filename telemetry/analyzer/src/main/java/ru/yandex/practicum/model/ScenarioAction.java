package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "scenario_actions")
@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@IdClass(ScenarioActionId.class)
public class ScenarioAction {
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
    @JoinColumn(name = "action_id")
    private Action action;
}

@Data
@NoArgsConstructor @AllArgsConstructor
class ScenarioActionId implements Serializable {
    private Long scenario;
    private String sensor;
    private Long action;
}
