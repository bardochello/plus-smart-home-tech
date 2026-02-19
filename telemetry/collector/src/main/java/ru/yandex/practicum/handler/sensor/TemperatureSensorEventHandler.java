package ru.yandex.practicum.handler.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.kafka.KafkaProducerEvent;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

import java.time.Instant;

@Slf4j
@Component
public class TemperatureSensorEventHandler implements SensorEventHandler {

    private final KafkaProducerEvent kafkaProducer;
    private final String topic;

    public TemperatureSensorEventHandler(KafkaProducerEvent kafkaProducer,
                                         @Value("${topic.sensor-events:telemetry.sensors.v1}") String topic) {
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        TemperatureSensorProto proto = event.getTemperatureSensor();
        long timestampMs = toMillis(event.getTimestamp());
        log.debug("Обработка события датчика температуры. ID: {}", event.getId());

        TemperatureSensorAvro payload = TemperatureSensorAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(timestampMs)
                .setTemperatureC(proto.getTemperatureC())
                .setTemperatureF(proto.getTemperatureF())
                .build();

        SensorEventAvro avro = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(timestampMs)
                .setPayload(payload)
                .build();

        kafkaProducer.send(topic, event.getId(), avro);
        log.debug("Событие датчика температуры отправлено в Kafka. ID: {}", event.getId());
    }

    private long toMillis(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).toEpochMilli();
    }
}
