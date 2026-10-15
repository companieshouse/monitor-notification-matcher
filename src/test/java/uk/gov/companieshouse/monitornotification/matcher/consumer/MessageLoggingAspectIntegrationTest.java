package uk.gov.companieshouse.monitornotification.matcher.consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import monitor.filing;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.buildFilingUpdateMessage;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class MessageLoggingAspectIntegrationTest {

    private static final WireMockServer wireMock = new WireMockServer(
            WireMockConfiguration.options().dynamicPort());

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        wireMock.start();
        registry.add("spring.internal.private.api.url", wireMock::baseUrl);
        registry.add("spring.internal.kafka.api.url", wireMock::baseUrl);
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @Autowired
    private NotificationMatchConsumer consumer;

    @Test
    void testAspectLogging(final CapturedOutput output) {
        Message<filing> message = buildFilingUpdateMessage();

        DataMapHolder.initialise(null);

        consumer.consume(message);

        String correlationId = DataMapHolder.getRequestId();

        // Verifies that the aspect methods were called
        assertTrue(output.getOut().contains("Processing kafka message"));
        assertTrue(output.getOut().contains(correlationId));
        assertTrue(output.getOut().contains("Processed kafka message"));

        assertThat(correlationId, is(notNullValue()));
    }
}
