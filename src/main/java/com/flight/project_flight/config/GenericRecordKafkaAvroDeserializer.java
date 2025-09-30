package com.flight.project_flight.config;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.avro.generic.GenericRecord;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.common.errors.SerializationException;

import java.util.Map;

public class GenericRecordKafkaAvroDeserializer implements Deserializer<GenericRecord> {

    private KafkaAvroDeserializer avroDeserializer;

    public GenericRecordKafkaAvroDeserializer() {
        avroDeserializer = new KafkaAvroDeserializer();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        avroDeserializer.configure(configs, isKey);  // Configure the internal KafkaAvroDeserializer
    }

    @Override
    public GenericRecord deserialize(String topic, byte[] data) {
        try {
            Object deserialized = avroDeserializer.deserialize(topic, data);
            if (deserialized instanceof GenericRecord) {
                return (GenericRecord) deserialized;
            } else {
                throw new SerializationException("Failed to deserialize Avro data into GenericRecord");
            }
        } catch (Exception e) {
            throw new SerializationException("Error during Avro deserialization", e);
        }
    }

    @Override
    public void close() {
        avroDeserializer.close();  // Close the internal deserializer
    }
}
