package uk.gov.companieshouse.monitornotification.matcher.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_CAUSE_FQCN;
import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_STACKTRACE;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.buildFilingUpdateMessage;

import consumer.exception.NonRetryableErrorException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import monitor.filing;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

@ExtendWith(MockitoExtension.class)
public class RetryableTopicErrorInterceptorTest {

    RetryableTopicErrorInterceptor underTest;

    @BeforeEach
    void setUp() {
        underTest = new RetryableTopicErrorInterceptor();
    }

    @Test
    void givenKafkaTopic_whenOnSend_thenReturnOriginalRecord() throws IOException {
        underTest.configure(Map.of());

        Message<filing> filingMessage = buildFilingUpdateMessage();
        ProducerRecord<String, Object> onSendRecord = new ProducerRecord<>("test-topic", "test-key", filingMessage.getPayload());

        ProducerRecord<String, Object> result = underTest.onSend(onSendRecord);

        underTest.onAcknowledgement(null, null);
        underTest.close();

        assertThat(result, is(notNullValue()));
        assertThat(result.topic(), is("test-topic"));
        assertThat(result.key(), is("test-key"));
        assertThat(result.value(), is(filingMessage.getPayload()));

        assertThat(onSendRecord, is(result));
    }

    @Test
    void givenErrorTopic_whenOnSend_thenReturnOriginalRecord() throws IOException {
        underTest.configure(Map.of());

        Message<filing> filingMessage = buildFilingUpdateMessage();
        ProducerRecord<String, Object> onSendRecord = new ProducerRecord<>("test-topic-error", "test-key", filingMessage.getPayload());

        ProducerRecord<String, Object> result = underTest.onSend(onSendRecord);

        underTest.onAcknowledgement(null, null);
        underTest.close();

        assertThat(result, is(notNullValue()));
        assertThat(result.topic(), is("test-topic-error"));
        assertThat(result.key(), is("test-key"));
        assertThat(result.value(), is(filingMessage.getPayload()));

        assertThat(onSendRecord, is(result));
    }

    @Test
    void givenValidExceptionCauseHeader_whenOnSend_thenReturnInvalidRecord() throws IOException {
        underTest.configure(Map.of());

        Message<filing> filingMessage = buildFilingUpdateMessage();

        List<Header> headers = List.of(
                new RecordHeader(EXCEPTION_CAUSE_FQCN, NonRetryableErrorException.class.getName().getBytes())
        );

        ProducerRecord<String, Object> onSendRecord = new ProducerRecord<>("test-topic-error", 1,
                System.currentTimeMillis(),"test-key", filingMessage.getPayload(), headers);

        ProducerRecord<String, Object> result = underTest.onSend(onSendRecord);

        underTest.onAcknowledgement(null, null);
        underTest.close();

        assertThat(result, is(notNullValue()));
        assertThat(result.topic(), is("test-topic-invalid"));
        assertThat(result.key(), is("test-key"));
        assertThat(result.value(), is(filingMessage.getPayload()));

        assertThat(onSendRecord, not(result));
    }

    @Test
    void givenInvalidExceptionCauseHeader_whenOnSend_thenReturnInvalidRecord() throws IOException {
        underTest.configure(Map.of());

        Message<filing> filingMessage = buildFilingUpdateMessage();

        List<Header> headers = List.of(
                new RecordHeader(EXCEPTION_CAUSE_FQCN, NonRetryableException.class.getName().getBytes())
        );

        ProducerRecord<String, Object> onSendRecord = new ProducerRecord<>("test-topic-error", 1,
                System.currentTimeMillis(),"test-key", filingMessage.getPayload(), headers);

        ProducerRecord<String, Object> result = underTest.onSend(onSendRecord);

        underTest.onAcknowledgement(null, null);
        underTest.close();

        assertThat(result, is(notNullValue()));
        assertThat(result.topic(), is("test-topic-error"));
        assertThat(result.key(), is("test-key"));
        assertThat(result.value(), is(filingMessage.getPayload()));

        assertThat(onSendRecord, is(result));
    }

    @Test
    void givenValidExceptionStacktraceHeader_whenOnSend_thenReturnInvalidRecord() throws IOException {
        underTest.configure(Map.of());

        Message<filing> filingMessage = buildFilingUpdateMessage();

        List<Header> headers = List.of(
                new RecordHeader(EXCEPTION_STACKTRACE, NonRetryableErrorException.class.getName().getBytes())
        );

        ProducerRecord<String, Object> onSendRecord = new ProducerRecord<>("test-topic-error", 1,
                System.currentTimeMillis(),"test-key", filingMessage.getPayload(), headers);

        ProducerRecord<String, Object> result = underTest.onSend(onSendRecord);

        underTest.onAcknowledgement(null, null);
        underTest.close();

        assertThat(result, is(notNullValue()));
        assertThat(result.topic(), is("test-topic-invalid"));
        assertThat(result.key(), is("test-key"));
        assertThat(result.value(), is(filingMessage.getPayload()));

        assertThat(onSendRecord, not(result));
    }

    @Test
    void givenInvalidExceptionStacktraceHeader_whenOnSend_thenReturnInvalidRecord() throws IOException {
        underTest.configure(Map.of());

        Message<filing> filingMessage = buildFilingUpdateMessage();

        List<Header> headers = List.of(
                new RecordHeader(EXCEPTION_STACKTRACE, NonRetryableException.class.getName().getBytes())
        );

        ProducerRecord<String, Object> onSendRecord = new ProducerRecord<>("test-topic-error", 1,
                System.currentTimeMillis(),"test-key", filingMessage.getPayload(), headers);

        ProducerRecord<String, Object> result = underTest.onSend(onSendRecord);

        underTest.onAcknowledgement(null, null);
        underTest.close();

        assertThat(result, is(notNullValue()));
        assertThat(result.topic(), is("test-topic-error"));
        assertThat(result.key(), is("test-key"));
        assertThat(result.value(), is(filingMessage.getPayload()));

        assertThat(onSendRecord, is(result));
    }
}

