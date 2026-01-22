package uk.gov.companieshouse.monitornotification.matcher.exception;

import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_CAUSE_FQCN;
import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_STACKTRACE;
import static uk.gov.companieshouse.monitornotification.matcher.config.ApplicationConfig.NAMESPACE;

import consumer.exception.NonRetryableErrorException;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;

public class RetryableTopicErrorInterceptor implements ProducerInterceptor<String, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Override
    public ProducerRecord<String, Object> onSend(final ProducerRecord<String, Object> record) {
        LOGGER.trace("onSend(record=%s) method called.".formatted(record));

        String nextTopic = record.topic().contains("-dlt-error") ? getNextErrorTopic(record) : record.topic();

        LOGGER.info(String.format("Moving record into new topic: %s with value: %s",
                        nextTopic, record.value()), DataMapHolder.getLogMap());

        if (nextTopic.contains("-invalid")) {
            return new ProducerRecord<>(nextTopic, record.key(), record.value());
        }

        return record;
    }

    @Override
    public void onAcknowledgement(final RecordMetadata recordMetadata, final Exception ex) {
        LOGGER.trace("onAcknowledgement(recordMetadata=%s, exception=%s) method called.".formatted(recordMetadata, ex));
    }

    @Override
    public void close() {
        LOGGER.trace("close() method called.");
    }

    @Override
    public void configure(final Map<String, ?> map) {
        LOGGER.trace("configure(map=%s) method called.".formatted(map));
    }

    private String getNextErrorTopic(ProducerRecord<String, Object> record) {
        LOGGER.trace("getNextErrorTopic(record=%s) method called.".formatted(record));

        Header header1 = record.headers().lastHeader(EXCEPTION_CAUSE_FQCN);
        Header header2 = record.headers().lastHeader(EXCEPTION_STACKTRACE);
        return ((header1 != null
                && new String(header1.value()).contains(NonRetryableErrorException.class.getName()))
                || (header2 != null
                && new String(header2.value()).contains(
                NonRetryableErrorException.class.getName())))
                ? record.topic().replace("-dlt-error", "-invalid") : record.topic();
    }
}

