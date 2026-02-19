package ru.yandex.practicum.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.KafkaProducerEvent;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ScenarioAddedEventHandler implements HubEventHandler {

    private final KafkaProducerEvent kafkaProducer;
    private final String topic;

    public ScenarioAddedEventHandler(KafkaProducerEvent kafkaProducer,
                                     @Value("${topic.hub-events:telemetry.hubs.v1}") String topic) {
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        ScenarioAddedEventProto proto = event.getScenarioAdded();
        log.debug("Обработка события добавления сценария. HubId: {}, Name: {}",
                event.getHubId(), proto.getName());

        List<ScenarioConditionAvro> conditions = proto.getConditionList().stream()
                .map(this::mapCondition)
                .collect(Collectors.toList());

        List<DeviceActionAvro> actions = proto.getActionList().stream()
                .map(this::mapAction)
                .collect(Collectors.toList());

        ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                .setName(proto.getName())
                .setConditions(conditions)
                .setActions(actions)
                .build();

        HubEventAvro avro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(toMillis(event.getTimestamp()))
                .setPayload(payload)
                .build();

        kafkaProducer.send(topic, event.getHubId(), avro);
        log.debug("Событие добавления сценария отправлено в Kafka. HubId: {}", event.getHubId());
    }

    private ScenarioConditionAvro mapCondition(ScenarioConditionProto proto) {
        Object value = null;
        switch (proto.getValueCase()) {
            case BOOL_VALUE:
                value = proto.getBoolValue();
                break;
            case INT_VALUE:
                value = proto.getIntValue();
                break;
            default:
                break;
        }

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(proto.getSensorId())
                .setType(ConditionTypeAvro.valueOf(proto.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(proto.getOperation().name()))
                .setValue(value)
                .build();
    }

    private DeviceActionAvro mapAction(DeviceActionProto proto) {
        Integer value = proto.hasValue() ? proto.getValue() : null;

        return DeviceActionAvro.newBuilder()
                .setSensorId(proto.getSensorId())
                .setType(ActionTypeAvro.valueOf(proto.getType().name()))
                .setValue(value)
                .build();
    }

    private long toMillis(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).toEpochMilli();
    }
}
