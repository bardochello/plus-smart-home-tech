package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class AggregationStarter {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final String sensorEventsTopic;
    private final String snapshotsTopic;
    private final SnapshotService snapshotService;

    public AggregationStarter(KafkaConfig kafkaConfig,
                              @Value("${kafka.topics.sensor-events}") String sensorEventsTopic,
                              @Value("${kafka.topics.snapshots}") String snapshotsTopic,
                              SnapshotService snapshotService) {
        this.consumer = new KafkaConsumer<>(kafkaConfig.getConsumerProperties());
        this.producer = new KafkaProducer<>(kafkaConfig.getProducerProperties());
        this.sensorEventsTopic = sensorEventsTopic;
        this.snapshotsTopic = snapshotsTopic;
        this.snapshotService = snapshotService;
    }

    public void start() {
        try {
            consumer.subscribe(List.of(sensorEventsTopic));
            log.info("Aggregator started.");

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));
                if (records.isEmpty()) continue;

                int processed = 0;
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    if (record.value() == null) continue;

                    snapshotService.updateState(record.value()).ifPresent(snapshot -> {
                        producer.send(new ProducerRecord<>(snapshotsTopic, snapshot.getHubId(), snapshot));
                    });
                    processed++;
                }
                log.info("Processed {} records", processed);

                producer.flush();
                consumer.commitSync();
            }
        } catch (Exception e) {
            log.error("Error in aggregator", e);
        } finally {
            consumer.close();
            producer.close();
        }
    }
}
