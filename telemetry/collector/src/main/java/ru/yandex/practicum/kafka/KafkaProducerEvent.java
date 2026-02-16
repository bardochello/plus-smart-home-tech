package ru.yandex.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.KafkaSendException;

import java.util.concurrent.Future;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducerEvent {

    private final Producer<String, SpecificRecordBase> kafkaProducer;

    /**
     * Отправляет сообщение в Kafka асинхронно с callback.
     */
    public void send(String topic, String key, SpecificRecordBase value) {
        validateParameters(topic, key, value);

        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, key, value);

        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                log.debug("Сообщение успешно отправлено в топик: '{}', ключ: '{}', partition: {}, offset: {}",
                        topic, key, metadata.partition(), metadata.offset());
            } else {
                log.error("Не удалось отправить сообщение в топик: '{}', ключ: '{}'",
                        topic, key, exception);
            }
        });
    }

    /**
     * Отправляет сообщение в Kafka синхронно.
     */
    public void sendSync(String topic, String key, SpecificRecordBase value) {
        validateParameters(topic, key, value);

        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, key, value);

        try {
            Future<RecordMetadata> future = kafkaProducer.send(record);
            RecordMetadata metadata = future.get();

            log.debug("Сообщение синхронно отправлено в топик: '{}', ключ: '{}', partition: {}, offset: {}",
                    topic, key, metadata.partition(), metadata.offset());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Поток был прерван при отправке сообщения. Топик: '{}', ключ: '{}'", topic, key, e);
            throw new KafkaSendException("Поток прерван при отправке сообщения в Kafka", e);
        } catch (Exception e) {
            log.error("Ошибка при синхронной отправке сообщения. Топик: '{}', ключ: '{}'", topic, key, e);
            throw new KafkaSendException("Не удалось отправить сообщение в Kafka", e);
        }
    }

    /**
     * Синхронизирует буферы продюсера.
     */
    public void flush() {
        kafkaProducer.flush();
        log.debug("Буферы Kafka продюсера успешно синхронизированы");
    }

    private void validateParameters(String topic, String key, SpecificRecordBase value) {
        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException("Топик не может быть null или пустым");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Ключ не может быть null или пустым");
        }
        if (value == null) {
            throw new IllegalArgumentException("Значение не может быть null");
        }

        log.trace("Параметры отправки валидны. Топик: '{}', ключ: '{}', тип значения: {}",
                topic, key, value.getClass().getSimpleName());
    }
}