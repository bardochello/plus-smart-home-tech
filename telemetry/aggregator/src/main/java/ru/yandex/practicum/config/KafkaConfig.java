package ru.yandex.practicum.config;

import ru.yandex.practicum.serializer.AvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.deserializer.SensorEventDeserializer;

import java.util.Properties;

@Configuration
public class KafkaConfig {

    @Value("${kafka.consumer.bootstrap-servers}")
    private String consumerBootstrapServers;

    @Value("${kafka.consumer.group-id}")
    private String groupId;

    @Value("${kafka.producer.bootstrap-servers}")
    private String producerBootstrapServers;

    public Properties getConsumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerBootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class.getName());
        // Важно: отключаем авто-коммит, так как будем делать commitAsync/commitSync вручную
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    public Properties getProducerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerBootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class.getName());
        return props;
    }
}
