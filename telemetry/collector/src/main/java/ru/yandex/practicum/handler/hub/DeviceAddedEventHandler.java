package ru.yandex.practicum.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.KafkaProducerEvent;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Slf4j
@Component
public class DeviceAddedEventHandler implements HubEventHandler {

    private final KafkaProducerEvent kafkaProducer;
    private final String topic;

    public DeviceAddedEventHandler(KafkaProducerEvent kafkaProducer,
                                   @Value("${topic.hub-events:telemetry.hubs.v1}") String topic) {
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        DeviceAddedEventProto proto = event.getDeviceAdded();
        log.debug("Обработка события добавления устройства. HubId: {}, DeviceId: {}",
                event.getHubId(), proto.getId());

        DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                .setId(proto.getId())
                .setType(DeviceTypeAvro.valueOf(proto.getType().name()))
                .build();

        HubEventAvro avro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(toMillis(event.getTimestamp()))
                .setPayload(payload)
                .build();

        kafkaProducer.send(topic, event.getHubId(), avro);
        log.debug("Событие добавления устройства отправлено в Kafka. HubId: {}", event.getHubId());
    }

    private long toMillis(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).toEpochMilli();
    }
}
