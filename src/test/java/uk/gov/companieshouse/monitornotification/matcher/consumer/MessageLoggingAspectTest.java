package uk.gov.companieshouse.monitornotification.matcher.consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.buildTransactionUpdateMessage;

import monitor.transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.monitornotification.matcher.util.DisabledIfDockerUnavailable;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DisabledIfDockerUnavailable
class MessageLoggingAspectTest {

    @Autowired
    private MonitorFilingConsumer consumer;

    @Test
    void testAspectLogging(CapturedOutput output) {
        Message<transaction> message = buildTransactionUpdateMessage();

        consumer.consume(message);

        // Verifies that the aspect methods were called
        assertTrue(output.getOut().contains("Processing kafka message"));
        assertTrue(output.getOut().contains("Processed kafka message"));
    }
}

