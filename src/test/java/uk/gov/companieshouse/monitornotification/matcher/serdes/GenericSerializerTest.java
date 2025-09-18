package uk.gov.companieshouse.monitornotification.matcher.serdes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.buildTransactionUpdateMessage;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.buildFilingUpdateMessage;

import consumer.exception.NonRetryableErrorException;
import monitor.filing;
import monitor.transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenericSerializerTest {

    GenericSerializer underTest;

    @BeforeEach
    void setUp() {
        underTest = new GenericSerializer();
    }

    @Test
    void givenTransactionPayload_whenSerialized_thenByteArrayCreated() {
        transaction payload = buildTransactionUpdateMessage().getPayload();

        byte[] result = underTest.serialize("test-topic", payload);

        assertThat(result, is(notNullValue()));
        assertThat(result.length, is(greaterThan(0)));
    }

    @Test
    void givenFilingPayload_whenSerialized_thenByteArrayCreated() {
        filing payload = buildFilingUpdateMessage().getPayload();

        byte[] result = underTest.serialize("test-topic", payload);

        assertThat(result, is(notNullValue()));
        assertThat(result.length, is(greaterThan(0)));
    }

    @Test
    void givenByteArrayPayload_whenSerialized_thenByteArrayCreated() {
        byte[] result = underTest.serialize("test-topic", new byte[]{1, 2, 3});

        assertThat(result, is(notNullValue()));
        assertThat(result.length, is(3));
    }

    @Test
    void givenStringPayload_whenSerialized_thenByteArrayCreated() {
        byte[] result = underTest.serialize("test-topic", "This is a test string");

        assertThat(result, is(notNullValue()));
        assertThat(result.length, is(21));
    }

    @Test
    void givenInvalidPayload_whenSerialized_thenExceptionThrown() {
        Object invalidPayload = mock(Object.class);
        when(invalidPayload.toString()).thenThrow(new RuntimeException("toString() error"));

        NonRetryableErrorException expectedException = assertThrows(NonRetryableErrorException.class, () -> {
            underTest.serialize("test-topic", invalidPayload);
        });

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("Serialization exception while writing to byte array"));
    }
}

