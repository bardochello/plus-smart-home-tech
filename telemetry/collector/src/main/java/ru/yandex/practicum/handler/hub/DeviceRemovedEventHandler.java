package ru.yandex.practicum.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.KafkaProducerEvent;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Slf4j
@Component
public class DeviceRemovedEventHandler implements HubEventHandler {

    private final KafkaProducerEvent kafkaProducer;
    private final String topic;

    public DeviceRemovedEventHandler(KafkaProducerEvent kafkaProducer,
                                     @Value("${topic.hub-events:telemetry.hubs.v1}") String topic) {
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        DeviceRemovedEventProto proto = event.getDeviceRemoved();
        log.debug("Обработка события удаления устройства. HubId: {}, DeviceId: {}",
                event.getHubId(), proto.getId());

        DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                .setId(proto.getId())
                .build();

        HubEventAvro avro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(toMillis(event.getTimestamp()))
                .setPayload(payload)
                .build();

        kafkaProducer.send(topic, event.getHubId(), avro);
        log.debug("Событие удаления устройства отправлено в Kafka. HubId: {}", event.getHubId());
    }

    private long toMillis(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).toEpochMilli();
    }
}
