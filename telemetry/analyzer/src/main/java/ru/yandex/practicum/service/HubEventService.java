package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventService {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    @Transactional
    public void processHubEvent(HubEventAvro event) {
        String hubId = event.getHubId();
        Object payload = event.getPayload();

        if (payload instanceof DeviceAddedEventAvro deviceAdded) {
            addDevice(hubId, deviceAdded);
        } else if (payload instanceof DeviceRemovedEventAvro deviceRemoved) {
            removeDevice(hubId, deviceRemoved);
        } else if (payload instanceof ScenarioAddedEventAvro scenarioAdded) {
            addScenario(hubId, scenarioAdded);
        } else if (payload instanceof ScenarioRemovedEventAvro scenarioRemoved) {
            removeScenario(hubId, scenarioRemoved);
        }
    }

    private void addDevice(String hubId, DeviceAddedEventAvro deviceAdded) {
        if (sensorRepository.findByIdAndHubId(deviceAdded.getId(), hubId).isEmpty()) {
            Sensor sensor = new Sensor();
            sensor.setId(deviceAdded.getId());
            sensor.setHubId(hubId);
            sensorRepository.save(sensor);
            log.info("Device added: {} to hub: {}", deviceAdded.getId(), hubId);
        }
    }

    private void removeDevice(String hubId, DeviceRemovedEventAvro deviceRemoved) {
        sensorRepository.findByIdAndHubId(deviceRemoved.getId(), hubId)
                .ifPresent(sensor -> {
                    sensorRepository.delete(sensor);
                    log.info("Device removed: {} from hub: {}", deviceRemoved.getId(), hubId);
                });
    }

    private void addScenario(String hubId, ScenarioAddedEventAvro scenarioAdded) {
        log.info("Processing ScenarioAdded: name={}, hubId={}", scenarioAdded.getName(), hubId);

        // Удаляем старый сценарий
        scenarioRepository.findByHubIdAndName(hubId, scenarioAdded.getName())
                .ifPresent(scenarioRepository::delete);

        Scenario scenario = new Scenario();
        scenario.setHubId(hubId);
        scenario.setName(scenarioAdded.getName());

        // Создаём условия
        List<ScenarioCondition> conditions = new ArrayList<>();
        for (ScenarioConditionAvro condAvro : scenarioAdded.getConditions()) {
            Sensor sensor = sensorRepository.findByIdAndHubId(condAvro.getSensorId(), hubId).orElse(null);
            if (sensor == null) {
                log.warn("Sensor {} not found for condition. Skipping.", condAvro.getSensorId());
                continue;
            }

            Condition condition = new Condition();
            condition.setType(condAvro.getType());
            condition.setOperation(condAvro.getOperation());

            Object val = condAvro.getValue();
            if (val instanceof Integer) {
                condition.setValue((Integer) val);
            } else if (val instanceof Boolean) {
                condition.setValue((Boolean) val ? 1 : 0);
            } else if (val != null) {
                condition.setValue(Integer.parseInt(val.toString()));
            }

            ScenarioCondition sc = new ScenarioCondition();
            sc.setScenario(scenario);
            sc.setSensor(sensor);
            sc.setCondition(condition);
            conditions.add(sc);
        }
        scenario.setConditions(conditions);

        // Создаём действия
        List<ScenarioAction> actions = new ArrayList<>();
        for (DeviceActionAvro actionAvro : scenarioAdded.getActions()) {
            Sensor sensor = sensorRepository.findByIdAndHubId(actionAvro.getSensorId(), hubId).orElse(null);
            if (sensor == null) {
                log.warn("Sensor {} not found for action. Skipping.", actionAvro.getSensorId());
                continue;
            }

            Action action = new Action();
            action.setType(actionAvro.getType());
            if (actionAvro.getValue() != null) {
                if (actionAvro.getValue() instanceof Integer) {
                    action.setValue((Integer) actionAvro.getValue());
                } else {
                    action.setValue(Integer.parseInt(actionAvro.getValue().toString()));
                }
            }

            ScenarioAction sa = new ScenarioAction();
            sa.setScenario(scenario);
            sa.setSensor(sensor);
            sa.setAction(action);
            actions.add(sa);
        }
        scenario.setActions(actions);

        scenarioRepository.save(scenario);
        log.info("Scenario saved: {} with {} conditions and {} actions",
                scenarioAdded.getName(), conditions.size(), actions.size());
    }

    private void removeScenario(String hubId, ScenarioRemovedEventAvro scenarioRemoved) {
        scenarioRepository.findByHubIdAndName(hubId, scenarioRemoved.getName())
                .ifPresent(scenario -> {
                    scenarioRepository.delete(scenario);
                    log.info("Scenario removed: {} from hub: {}", scenarioRemoved.getName(), hubId);
                });
    }
}
