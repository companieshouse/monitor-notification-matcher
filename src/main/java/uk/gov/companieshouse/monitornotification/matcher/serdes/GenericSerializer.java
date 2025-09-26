package uk.gov.companieshouse.monitornotification.matcher.serdes;

import static java.lang.String.format;
import static uk.gov.companieshouse.monitornotification.matcher.config.ApplicationConfig.NAMESPACE;

import consumer.exception.NonRetryableErrorException;
import java.nio.charset.StandardCharsets;
import monitor.filing;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;

public class GenericSerializer implements Serializer<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Override
    public byte[] serialize(final String topic, final Object payload) {
        LOGGER.trace(format("serialize() -> [Topic: %s, Payload: %s]", topic, payload.getClass().getSimpleName()));

        try {
            if (payload instanceof byte[]) {
                return (byte[]) payload;
            }

            if (payload instanceof filing notificationMatch) {
                DatumWriter<filing> writer = new SpecificDatumWriter<>();
                var encoderFactory = EncoderFactory.get();

                AvroSerializer<filing> avroSerializer = new AvroSerializer<>(writer, encoderFactory);
                return avroSerializer.toBinary(notificationMatch);
            }

            return payload.toString().getBytes(StandardCharsets.UTF_8);

        } catch (Exception ex) {
            LOGGER.error("Serialization exception while writing to byte array", ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException("Serialization exception while writing to byte array", ex);
        }
    }
}

