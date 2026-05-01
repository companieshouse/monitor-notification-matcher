package uk.gov.companieshouse.monitornotification.matcher.consumer;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import monitor.filing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.monitornotification.matcher.logging.DataMapHolder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.buildFilingUpdateMessage;

@SpringBootTest
@WireMockTest(httpPort = 4081)
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class MessageLoggingAspectIntegrationTest {

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
