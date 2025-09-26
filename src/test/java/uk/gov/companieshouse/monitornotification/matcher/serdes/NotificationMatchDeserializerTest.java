package uk.gov.companieshouse.monitornotification.matcher.serdes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.KIND;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.NOTIFIED_AT;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.USER_ID;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.buildFilingRawAvroMessage;

import consumer.exception.NonRetryableErrorException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import monitor.filing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NotificationMatchDeserializerTest {

    NotificationMatchDeserializer underTest;

    @BeforeEach
    public void setUp() {
        underTest = new NotificationMatchDeserializer();
    }

    @Test
    public void givenValidPayload_whenDeserialized_thenSuccessReturned() throws IOException {
        byte[] payload = buildFilingRawAvroMessage();

        filing result = underTest.deserialize("test-topic", payload);

        assertThat(result, is(notNullValue()));
        assertThat(result.getData(), is(notNullValue()));
        assertThat(result.getKind(), is(KIND));
        assertThat(result.getNotifiedAt(), is(NOTIFIED_AT));
        assertThat(result.getUserId(), is(USER_ID));
    }

    @Test
    public void givenInvalidPayload_whenDeserialized_thenExceptionRaised() {
        String payload = "This string won't deserialize";

        NonRetryableErrorException expectedException = assertThrows(NonRetryableErrorException.class, () -> {
            underTest.deserialize("test-topic", payload.getBytes(StandardCharsets.UTF_8));
        });

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("De-Serialization exception while converting to Avro schema object"));
    }
}