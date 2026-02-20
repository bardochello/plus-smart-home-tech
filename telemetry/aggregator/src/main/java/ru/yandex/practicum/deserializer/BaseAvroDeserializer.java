package ru.yandex.practicum.deserializer;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private final DatumReader<T> datumReader;

    public BaseAvroDeserializer(Schema schema) {
        this.datumReader = new SpecificDatumReader<>(schema);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            if (data == null) {
                return null;
            }
            BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
            return datumReader.read(null, decoder);
        } catch (IOException e) {
            throw new SerializationException("Error deserializing Avro message for topic " + topic, e);
        }
    }
}
