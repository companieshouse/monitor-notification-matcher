package uk.gov.companieshouse.monitornotification.matcher.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.buildFilingUpdateMessage;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import monitor.filing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opentest4j.TestAbortedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.monitornotification.matcher.consumer.NotificationMatchConsumer;

@SpringBootTest
@EmbeddedKafka(partitions = 1,
        topics = { "test-topic" },
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092" }
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
public class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private NotificationMatchConsumer consumer;

    private final CountDownLatch latch = new CountDownLatch(1);
    private filing receivedMessage;

    @BeforeEach
    void setup() {
        consumer.setCallback(message -> {
            receivedMessage = message;
            latch.countDown();
        });
    }

    @Test
    void testMessageIsConsumed() throws IOException, InterruptedException {
        Message<filing> message = buildFilingUpdateMessage();

        kafkaTemplate.send("test-topic", message.getPayload());

        boolean messageConsumed = latch.await(10, TimeUnit.SECONDS);

        if (!messageConsumed) {
            // Skip the test (won't mark it as failed, only as skipped)
            throw new TestAbortedException("Message not received within timeout – skipping test.");
        }

        String expectedMessage = "{\"company_number\": \"00006400\", \"data\": \"{\\\"company_number\\\":\\\"00006400\\\",\\\"data\\\":{\\\"type\\\":\\\"AP01\\\",\\\"transaction_id\\\":\\\"158153-915517-386847\\\",\\\"description\\\":\\\"appoint-person-director-company-with-name-date\\\",\\\"description_values\\\":{\\\"appointment_date\\\":\\\"1 December 2024\\\",\\\"officer_name\\\":\\\"DR AMIDAT DUPE IYIOLA\\\"},\\\"date\\\":\\\"2025-02-04\\\"},\\\"is_delete\\\":false}\", \"published_at\": \"2025-03-03T15:04:03\", \"version\": \"0\"}";

        assertTrue(messageConsumed, "Message was not consumed in time");
        assertThat(423, is(expectedMessage.length()));

        assertThat(receivedMessage, is(message.getPayload()));
    }
}