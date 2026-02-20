package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class AggregationStarter {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final String sensorEventsTopic;
    private final String snapshotsTopic;
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public AggregationStarter(KafkaConfig kafkaConfig,
                              @Value("${kafka.topics.sensor-events}") String sensorEventsTopic,
                              @Value("${kafka.topics.snapshots}") String snapshotsTopic) {
        this.consumer = new KafkaConsumer<>(kafkaConfig.getConsumerProperties());
        this.producer = new KafkaProducer<>(kafkaConfig.getProducerProperties());
        this.sensorEventsTopic = sensorEventsTopic;
        this.snapshotsTopic = snapshotsTopic;
    }

    public void start() {
        try {
            consumer.subscribe(List.of(sensorEventsTopic));
            log.info("Aggregator started. Subscribed to: {}", sensorEventsTopic);
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();
                    try {
                        Optional<SensorsSnapshotAvro> snapshotOpt = updateState(event);

                        if (snapshotOpt.isPresent()) {
                            SensorsSnapshotAvro snapshot = snapshotOpt.get();
                            ProducerRecord<String, SensorsSnapshotAvro> producerRecord =
                                    new ProducerRecord<>(snapshotsTopic, snapshot.getHubId(), snapshot);
                            producer.send(producerRecord);
                            log.info("Snapshot sent for hub: {} due to sensor: {}", snapshot.getHubId(), event.getId());
                        }
                    } catch (Exception e) {
                        log.error("Error processing event: {}", event, e);
                    }
                }
                consumer.commitAsync();
            }
        } catch (WakeupException e) {
            log.info("Aggregator stopping...");
        } catch (Exception e) {
            log.error("Unexpected error in Aggregator loop", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                consumer.close();
                producer.close();
                log.info("Aggregator closed resources.");
            }
        }
    }

    private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        Instant eventTimestamp = Instant.ofEpochMilli(event.getTimestamp());

        SensorsSnapshotAvro snapshot = snapshots.get(hubId);

        if (snapshot == null) {
            snapshot = new SensorsSnapshotAvro();
            snapshot.setHubId(hubId);
            snapshot.setTimestamp(eventTimestamp);
            snapshot.setSensorsState(new HashMap<>());
            snapshots.put(hubId, snapshot);

            // Новое состояние датчика
            updateSensorState(snapshot, event, eventTimestamp);
            return Optional.of(snapshot);
        }

        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());

        if (oldState != null) {
            if (oldState.getTimestamp().isAfter(eventTimestamp)) {
                return Optional.empty();
            }

            if (oldState.getTimestamp().equals(eventTimestamp) &&
                    oldState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }

        updateSensorState(snapshot, event, eventTimestamp);

        if (snapshot.getTimestamp().isBefore(eventTimestamp)) {
            snapshot.setTimestamp(eventTimestamp);
        }

        return Optional.of(snapshot);
    }

    private void updateSensorState(SensorsSnapshotAvro snapshot, SensorEventAvro event, Instant timestamp) {
        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(timestamp);
        newState.setData(event.getPayload());
        snapshot.getSensorsState().put(event.getId(), newState);
    }
}
