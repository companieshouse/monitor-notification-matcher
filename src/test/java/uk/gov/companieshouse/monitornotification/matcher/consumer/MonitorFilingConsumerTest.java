package uk.gov.companieshouse.monitornotification.matcher.consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.buildTransactionDeleteMessage;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.buildTransactionUpdateMessage;

import java.io.IOException;
import monitor.transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.exception.RetryableException;
import uk.gov.companieshouse.monitornotification.matcher.service.MatcherService;

@ExtendWith(MockitoExtension.class)
public class MonitorFilingConsumerTest {

    @Mock
    MatcherService service;

    @Mock
    MessageFlags flags;

    @Mock
    Logger logger;

    @InjectMocks
    MonitorFilingConsumer underTest;

    @BeforeEach
    void setUp() {
        // nothing
    }

    @Test
    @DisplayName("Given a valid update message, when consumed, then message is successful")
    void givenValidUpdateMessage_whenConsumed_thenMessageSuccessful() throws IOException {
        Message<transaction> transactionMessage = buildTransactionUpdateMessage();

        underTest.consume(transactionMessage);

        verify(logger, times(1)).debug(anyString());
        verify(service, times(1)).processMessage(transactionMessage.getPayload());
    }

    @Test
    @DisplayName("Given a valid delete message, when consumed, then message is successful")
    void givenValidDeleteMessage_whenConsumed_thenMessageSuccessful() throws IOException {
        Message<transaction> transactionMessage = buildTransactionDeleteMessage();

        underTest.consume(transactionMessage);

        verify(logger, times(1)).debug(anyString());
        verify(service, times(1)).processMessage(transactionMessage.getPayload());
    }

    @Test
    @DisplayName("Given an invalid JSON payload, when consumed, then non-retryable exception is thrown")
    void givenInvalidMessage_whenConsumed_thenRaiseNonRetryableException() throws IOException {
        Message<transaction> transactionMessage = buildTransactionDeleteMessage();
        doThrow(new NonRetryableException("test exception")).when(service).processMessage(transactionMessage.getPayload());

        NonRetryableException expectedException = assertThrows(NonRetryableException.class, () -> {
            underTest.consume(transactionMessage);
        });

        verify(logger, times(1)).debug(anyString());
        verify(flags, times(1)).setRetryable(false);
        verify(service, times(1)).processMessage(transactionMessage.getPayload());

        assertThat(expectedException.getMessage(), is("test exception"));
    }

    @Test
    @DisplayName("Given an invalid JSON payload, when consumed, then retryable exception is thrown")
    void givenInvalidMessage_whenConsumed_thenRaiseRetryableException() throws IOException {
        Message<transaction> transactionMessage = buildTransactionDeleteMessage();
        doThrow(new RetryableException("test exception", new RuntimeException())).when(service).processMessage(transactionMessage.getPayload());

        RetryableException expectedException = assertThrows(RetryableException.class, () -> {
            underTest.consume(transactionMessage);
        });

        verify(logger, times(1)).debug(anyString());
        verify(flags, times(1)).setRetryable(true);
        verify(service, times(1)).processMessage(transactionMessage.getPayload());

        assertThat(expectedException.getMessage(), is("test exception"));
    }

}

