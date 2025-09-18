package uk.gov.companieshouse.monitornotification.matcher.serdes;

import static uk.gov.companieshouse.monitornotification.matcher.config.ApplicationConfig.NAMESPACE;

import consumer.exception.NonRetryableErrorException;
import java.io.IOException;
import monitor.transaction;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;

public class MonitorFilingDeserializer implements Deserializer<transaction> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Override
    public transaction deserialize(String topic, byte[] data) {
        LOGGER.trace("deserialize() -> [Topic: %s, Data: %d bytes]".formatted(topic, data.length));

        try {
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            DatumReader<transaction> reader = new ReflectDatumReader<>(transaction.class);
            transaction record = reader.read(null, decoder);

            LOGGER.info("Message successfully de-serialised", DataMapHolder.getLogMap());

            return record;

        } catch (IOException | AvroRuntimeException ex) {
            LOGGER.error("De-Serialization exception while converting to Avro schema object", ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException("De-Serialization exception while converting to Avro schema object", ex);
        }
    }
}

