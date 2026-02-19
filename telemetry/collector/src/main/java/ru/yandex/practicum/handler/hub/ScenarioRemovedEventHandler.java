package ru.yandex.practicum.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;
import ru.yandex.practicum.kafka.KafkaProducerEvent;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

import java.time.Instant;

@Slf4j
@Component
public class ScenarioRemovedEventHandler implements HubEventHandler {

    private final KafkaProducerEvent kafkaProducer;
    private final String topic;

    public ScenarioRemovedEventHandler(KafkaProducerEvent kafkaProducer,
                                       @Value("${topic.hub-events:telemetry.hubs.v1}") String topic) {
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        ScenarioRemovedEventProto proto = event.getScenarioRemoved();
        log.debug("Обработка события удаления сценария. HubId: {}, Name: {}",
                event.getHubId(), proto.getName());

        ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                .setName(proto.getName())
                .build();

        HubEventAvro avro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(toMillis(event.getTimestamp()))
                .setPayload(payload)
                .build();

        kafkaProducer.send(topic, event.getHubId(), avro);
        log.debug("Событие удаления сценария отправлено в Kafka. HubId: {}", event.getHubId());
    }

    private long toMillis(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).toEpochMilli();
    }
}
