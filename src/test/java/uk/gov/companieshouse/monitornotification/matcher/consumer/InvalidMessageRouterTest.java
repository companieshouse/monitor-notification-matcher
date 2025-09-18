package uk.gov.companieshouse.monitornotification.matcher.consumer;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.buildTransactionUpdateMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import monitor.transaction;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

@ExtendWith(MockitoExtension.class)
public class InvalidMessageRouterTest {

    private InvalidMessageRouter underTest;

    @BeforeEach
    public void setUp() {
        underTest = new InvalidMessageRouter();
    }

    @Test
    public void givenRetryableMessage_whenOnSendCalled_thenOriginalRecordReturned() throws IOException {
        MessageFlags messageFlags = new MessageFlags();
        messageFlags.setRetryable(true);

        Map<String, Object> configs = new HashMap<>();
        configs.put("message.flags", messageFlags);
        configs.put("invalid.message.topic", "invalid-test-topic");
        underTest.configure(configs);

        Message<transaction> transactionMessage = buildTransactionUpdateMessage();
        ProducerRecord<String, transaction> onSendRecord = new ProducerRecord<>("test-topic", "test-key", transactionMessage.getPayload());

        ProducerRecord<String, transaction> result = underTest.onSend(onSendRecord);

        assertThat(result.topic(), is("test-topic"));
        assertThat(result.key(), is("test-key"));
        assertThat(result.value(), is(transactionMessage.getPayload()));

        assertThat(onSendRecord, is(result));
    }

    @Test
    public void givenNonRetryableMessage_whenOnSendCalled_thenInvalidTopicRecordReturned() throws IOException {
        MessageFlags messageFlags = new MessageFlags();
        messageFlags.setRetryable(false);

        Map<String, Object> configs = new HashMap<>();
        configs.put("message.flags", messageFlags);
        configs.put("invalid.message.topic", "invalid-test-topic");
        underTest.configure(configs);

        Message<transaction> transactionMessage = buildTransactionUpdateMessage();
        ProducerRecord<String, transaction> onSendRecord = new ProducerRecord<>("test-topic", "test-key", transactionMessage.getPayload());

        ProducerRecord<String, transaction> result = underTest.onSend(onSendRecord);

        assertThat(result.topic(), is("invalid-test-topic"));
        assertThat(result.key(), is("test-key"));
        assertThat(result.value(), is(transactionMessage.getPayload()));

        assertThat(onSendRecord, not(result));
    }

    @Test
    public void givenNullArguments_whenOnAcknowledgementCalled_thenNothingHappens() {
        underTest.onAcknowledgement(null, null);
    }

    @Test
    public void givenNoArguments_whenOnCloseCalled_thenNothingHappens() {
        underTest.close();
    }
}

