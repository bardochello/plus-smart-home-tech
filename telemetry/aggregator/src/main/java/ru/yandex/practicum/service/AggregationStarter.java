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

    // Хранилище снапшотов в памяти (можно вынести в отдельный Repository класс, но можно и тут)
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

            // Хук для корректной остановки при завершении приложения
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();
                    try {
                        Optional<SensorsSnapshotAvro> snapshotOpt = updateState(event);

                        // Если состояние изменилось, отправляем снапшот
                        if (snapshotOpt.isPresent()) {
                            SensorsSnapshotAvro snapshot = snapshotOpt.get();
                            ProducerRecord<String, SensorsSnapshotAvro> producerRecord =
                                    new ProducerRecord<>(snapshotsTopic, snapshot.getHubId(), snapshot);

                            producer.send(producerRecord);
                            log.debug("Snapshot sent for hub: {}", snapshot.getHubId());
                        }
                    } catch (Exception e) {
                        log.error("Error processing event: {}", event, e);
                    }
                }
                // Асинхронный коммит оффсетов, чтобы не блокировать цикл
                consumer.commitAsync();
            }
        } catch (WakeupException e) {
            log.info("Aggregator stopping...");
        } catch (Exception e) {
            log.error("Unexpected error in Aggregator loop", e);
        } finally {
            try {
                // Перед закрытием сбрасываем данные продюсера и фиксируем оффсеты консьюмера
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
        // В Avro generated classes timestamp_ms мапится на Instant, если включены logical types,
        // но входящее событие SensorEventAvro имеет long timestamp. Конвертируем:
        Instant eventTimestamp = Instant.ofEpochMilli(event.getTimestamp());

        // 1. Достаем или создаем снапшот
        SensorsSnapshotAvro snapshot = snapshots.get(hubId);

        if (snapshot == null) {
            snapshot = new SensorsSnapshotAvro();
            snapshot.setHubId(hubId);
            snapshot.setTimestamp(eventTimestamp);
            snapshot.setSensorsState(new HashMap<>());
            snapshots.put(hubId, snapshot);
        }

        // 2. Достаем старое состояние датчика
        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());

        // 3. Проверяем актуальность
        if (oldState != null) {
            // Если пришедшее событие старее текущего состояния или данные не изменились -> игнорируем
            if (oldState.getTimestamp().isAfter(eventTimestamp) || oldState.getTimestamp().equals(eventTimestamp)) {
                return Optional.empty();
            }
            if (oldState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }

        // 4. Обновляем состояние
        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(eventTimestamp);
        newState.setData(event.getPayload());

        snapshot.getSensorsState().put(event.getId(), newState);
        snapshot.setTimestamp(eventTimestamp); // Обновляем время самого снапшота

        return Optional.of(snapshot);
    }
}
