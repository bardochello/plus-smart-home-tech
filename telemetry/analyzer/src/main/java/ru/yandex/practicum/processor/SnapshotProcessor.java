package ru.yandex.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.ScenarioAction;
import ru.yandex.practicum.model.ScenarioCondition;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.service.HubRouterClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SnapshotProcessor implements Runnable {

    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final String topic;
    private final ScenarioRepository scenarioRepository;
    private final HubRouterClient hubRouterClient;

    public SnapshotProcessor(KafkaConfig kafkaConfig,
                             @Value("${kafka.topics.snapshots}") String topic,
                             ScenarioRepository scenarioRepository,
                             HubRouterClient hubRouterClient) {
        this.consumer = new KafkaConsumer<>(kafkaConfig.getSnapshotConsumerProperties());
        this.topic = topic;
        this.scenarioRepository = scenarioRepository;
        this.hubRouterClient = hubRouterClient;
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(List.of(topic));
            log.info("SnapshotProcessor started. Subscribed to: {}", topic);

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    processSnapshot(record.value());
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            log.info("SnapshotProcessor stopping...");
        } finally {
            consumer.close();
        }
    }

    public void stop() {
        consumer.wakeup();
    }

    private void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        log.info("Processing snapshot for hub {}. Scenarios: {}, Sensors: {}", hubId, scenarios.size(), sensorsState.size());

        for (Scenario scenario : scenarios) {
            if (checkConditions(scenario.getConditions(), sensorsState)) {
                executeActions(scenario, hubId);
            }
        }
    }

    private boolean checkConditions(List<ScenarioCondition> conditions, Map<String, SensorStateAvro> sensorsState) {
        for (ScenarioCondition sc : conditions) {
            String sensorId = sc.getSensor().getId();
            SensorStateAvro state = sensorsState.get(sensorId);
            if (state == null) return false;

            if (!evaluateCondition(sc.getCondition(), state)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateCondition(Condition condition, SensorStateAvro state) {
        Object sensorValue = extractValue(state, condition.getType());

        if (sensorValue == null) return false;

        // Поддержка boolean и int
        Comparable actual = null;
        Comparable target = condition.getValue();

        if (sensorValue instanceof Boolean) {
            actual = (Boolean) sensorValue ? 1 : 0;
        } else if (sensorValue instanceof Number) {
            actual = ((Number) sensorValue).doubleValue();
            target = condition.getValue().doubleValue();
        } else {
            return false;
        }

        log.debug("Eval: actual={}, op={}, target={}", actual, condition.getOperation(), target);

        switch (condition.getOperation()) {
            case EQUALS:
                return actual.compareTo(target) == 0;
            case GREATER_THAN:
                return actual.compareTo(target) > 0;
            case LOWER_THAN:
                return actual.compareTo(target) < 0;
            default:
                return false;
        }
    }

    private Object extractValue(SensorStateAvro state, ConditionTypeAvro type) {
        Object data = state.getData();
        switch (type) {
            case MOTION:
                if (data instanceof MotionSensorAvro motion) return motion.getMotion();
                break;
            case LUMINOSITY:
                if (data instanceof LightSensorAvro light) return light.getLuminosity();
                break;
            case SWITCH:
                if (data instanceof SwitchSensorAvro sw) return sw.getState();
                break;
            case TEMPERATURE:
                if (data instanceof TemperatureSensorAvro temp) return temp.getTemperatureC();
                if (data instanceof ClimateSensorAvro clim) return clim.getTemperatureC();
                break;
            case CO2LEVEL:
                if (data instanceof ClimateSensorAvro clim) return clim.getCo2Level();
                break;
            case HUMIDITY:
                if (data instanceof ClimateSensorAvro clim) return clim.getHumidity();
                break;
        }
        return null;
    }

    private void executeActions(Scenario scenario, String hubId) {
        log.info("Scenario triggered: {} for hub {}", scenario.getName(), hubId);
        for (ScenarioAction sa : scenario.getActions()) {
            hubRouterClient.sendAction(hubId, scenario.getName(), sa.getSensor().getId(), sa.getAction());
        }
    }
}
