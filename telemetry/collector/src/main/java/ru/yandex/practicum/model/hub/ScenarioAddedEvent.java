package ru.yandex.practicum.model.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioAddedEvent extends HubEvent {

    @NotBlank(message = "Название сценария не может быть пустым")
    private String name;

    @NotEmpty(message = "Список условий не может быть пустым")
    private List<ScenarioCondition> conditions;

    @NotEmpty(message = "Список действий не может быть пустым")
    private List<DeviceAction> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}
