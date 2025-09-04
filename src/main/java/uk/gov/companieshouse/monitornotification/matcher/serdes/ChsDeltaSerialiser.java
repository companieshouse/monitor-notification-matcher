package uk.gov.companieshouse.monitornotification.matcher.serdes;

import static uk.gov.companieshouse.monitornotification.matcher.config.ApplicationConfig.NAMESPACE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;

public class ChsDeltaSerialiser implements Serializer<ChsDelta> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Override
    public byte[] serialize(final String topic, final ChsDelta data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<ChsDelta> writer = getDatumWriter();
        try {
            writer.write(data, encoder);

        } catch (IOException ex) {
            throw new NonRetryableException("Error serialising delta", ex);
        }
        return outputStream.toByteArray();
    }

    public DatumWriter<ChsDelta> getDatumWriter() {
        return new ReflectDatumWriter<>(ChsDelta.class);
    }
}
