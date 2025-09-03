package uk.gov.companieshouse.monitornotification.matcher.kafka;

import static java.lang.String.format;
import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_MESSAGE;
import static org.springframework.kafka.support.KafkaHeaders.ORIGINAL_OFFSET;
import static org.springframework.kafka.support.KafkaHeaders.ORIGINAL_PARTITION;
import static org.springframework.kafka.support.KafkaHeaders.ORIGINAL_TOPIC;
import static uk.gov.companieshouse.monitornotification.matcher.Application.NAMESPACE;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;

public class InvalidMessageRouter implements ProducerInterceptor<String, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private MessageFlags messageFlags;
    private String invalidTopic;

    @Override
    public ProducerRecord<String, Object> onSend(final ProducerRecord<String, Object> record) {
        LOGGER.trace(format("onSend(record=%s) method called.", record));

        if (messageFlags.isRetryable()) {
            messageFlags.destroy();
            return record;

        } else {

            String originalTopic = Optional.ofNullable(record.headers().lastHeader(ORIGINAL_TOPIC))
                    .map(h -> new String(h.value())).orElse(record.topic());
            BigInteger partition = Optional.ofNullable(record.headers().lastHeader(ORIGINAL_PARTITION))
                    .map(h -> new BigInteger(h.value())).orElse(BigInteger.valueOf(-1));
            BigInteger offset = Optional.ofNullable(record.headers().lastHeader(ORIGINAL_OFFSET))
                    .map(h -> new BigInteger(h.value())).orElse(BigInteger.valueOf(-1));
            String exception = Optional.ofNullable(record.headers().lastHeader(EXCEPTION_MESSAGE))
                    .map(h -> new String(h.value())).orElse("unknown");

            LOGGER.error("""
                    Republishing record to topic: [%s] \
                    From: original topic: [%s], partition: [%s], offset: [%s], exception: [%s]\
                    """.formatted(invalidTopic, originalTopic, partition, offset, exception),
                    DataMapHolder.getLogMap());

            return new ProducerRecord<>(invalidTopic, record.key(), record.value());
        }
    }

    @Override
    public void onAcknowledgement(final RecordMetadata metadata, final Exception exception) {
        LOGGER.trace(format("onAcknowledgement(metadata=%s, exception=%s) method called.", metadata, exception));
    }

    @Override
    public void close() {
        LOGGER.trace("close() method called.");
    }

    @Override
    public void configure(final Map<String, ?> configs) {
        LOGGER.trace(format("configure(configs=%s) method called.", configs));

        this.messageFlags = (MessageFlags) configs.get("message-flags");
        this.invalidTopic = (String) configs.get("invalid-topic");
    }
}
