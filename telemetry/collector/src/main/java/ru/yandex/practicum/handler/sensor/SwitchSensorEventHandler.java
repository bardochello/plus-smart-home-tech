package ru.yandex.practicum.handler.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.kafka.KafkaProducerEvent;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

import java.time.Instant;

@Slf4j
@Component
public class SwitchSensorEventHandler implements SensorEventHandler {

    private final KafkaProducerEvent kafkaProducer;
    private final String topic;

    public SwitchSensorEventHandler(KafkaProducerEvent kafkaProducer,
                                    @Value("${topic.sensor-events:telemetry.sensors.v1}") String topic) {
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        SwitchSensorProto proto = event.getSwitchSensor();
        log.debug("Обработка события датчика-переключателя. ID: {}", event.getId());

        SwitchSensorAvro payload = SwitchSensorAvro.newBuilder()
                .setState(proto.getState())
                .build();

        SensorEventAvro avro = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(toMillis(event.getTimestamp()))
                .setPayload(payload)
                .build();

        kafkaProducer.send(topic, event.getId(), avro);
        log.debug("Событие датчика-переключателя отправлено в Kafka. ID: {}", event.getId());
    }

    private long toMillis(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).toEpochMilli();
    }
}
