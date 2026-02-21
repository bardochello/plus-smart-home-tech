package ru.yandex.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.HubEventService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final String topic;
    private final HubEventService hubEventService;

    public HubEventProcessor(KafkaConfig kafkaConfig,
                             @Value("${kafka.topics.hub-events}") String topic,
                             HubEventService hubEventService) {
        this.consumer = new KafkaConsumer<>(kafkaConfig.getHubEventConsumerProperties());
        this.topic = topic;
        this.hubEventService = hubEventService;
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(List.of(topic));
            log.info("HubEventProcessor started. Subscribed to: {}", topic);

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    hubEventService.processHubEvent(record.value());
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            log.info("HubEventProcessor stopping...");
        } finally {
            consumer.close();
        }
    }

    public void stop() {
        consumer.wakeup();
    }
}
