package uk.gov.companieshouse.monitornotification.matcher.service;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.COMPANY_NUMBER;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.TRANSACTION_ID;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.buildTransactionDeleteMessageWithoutTransactionID;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.buildTransactionEmptyDataMessage;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.buildTransactionInvalidMessage;
import static uk.gov.companieshouse.monitornotification.matcher.util.MonitorFilingTestUtils.buildTransactionUpdateMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import monitor.filing;
import monitor.transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.converter.TransactionToFilingConverter;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.producer.NotificationMatchProducer;
import uk.gov.companieshouse.monitornotification.matcher.repository.MonitorRepository;
import uk.gov.companieshouse.monitornotification.matcher.repository.model.MonitorQueryDocument;

@ExtendWith(MockitoExtension.class)
class MatcherServiceTest {

    @Mock
    MonitorRepository repository;

    @Mock
    NotificationMatchProducer producer;

    @Mock
    Logger logger;

    TransactionToFilingConverter converter;

    MatcherService underTest;

    @BeforeEach
    void setUp() {
        converter = new TransactionToFilingConverter();

        underTest = new MatcherService(repository, new ObjectMapper(), producer, converter, logger);
    }

    @Test
    void givenNoMatches_whenProcessed_thenNoMessagesProduced() {
        Message<transaction> message = buildTransactionUpdateMessage();

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Collections.emptyList());

        underTest.processMessage(message.getPayload());

        verify(logger, times(2)).trace(anyString());
        verify(logger, times(1)).debug("Received transaction for company: [%s], transaction id: [%s] - attempting to match users".
                formatted(COMPANY_NUMBER, TRANSACTION_ID));
        verify(repository, times(1)).findByCompanyNumber(COMPANY_NUMBER);

        verifyNoInteractions(producer);
    }

    @Test
    void givenMultipleMatches_whenProcessed_thenMultipleMessagesProduced() {
        Message<transaction> message = buildTransactionUpdateMessage();

        List<MonitorQueryDocument> documents = createQueryDocuments();
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(documents);

        underTest.processMessage(message.getPayload());

        verify(logger, times(2)).trace(anyString());
        verify(logger, times(1)).debug("Received transaction for company: [%s], transaction id: [%s] - attempting to match users".
                formatted(COMPANY_NUMBER, TRANSACTION_ID));
        verify(repository, times(1)).findByCompanyNumber(COMPANY_NUMBER);

        verify(producer, times(documents.size())).sendMessage(any(filing.class));
    }

    @Test
    void givenMultipleMatchesWithoutTransactionID_whenProcessed_thenMultipleMessagesProduced() {
        Message<transaction> message = buildTransactionDeleteMessageWithoutTransactionID();

        List<MonitorQueryDocument> documents = createQueryDocuments();
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(documents);

        underTest.processMessage(message.getPayload());

        verify(logger, times(1)).trace("processMessage(message=%s) method called.".formatted(message.getPayload()));
        verify(logger, times(1)).debug("Delete non-existent filing for company: [%s] received - attempting to match users".
                formatted(COMPANY_NUMBER));
        verify(repository, times(1)).findByCompanyNumber(COMPANY_NUMBER);

        verify(producer, times(documents.size())).sendMessage(any(filing.class));
    }

    @Test
    void givenInvalidPayload_whenFetchingTransactionID_thenRaiseException() {
        Message<transaction> message = buildTransactionInvalidMessage();
        transaction payload = message.getPayload();

        NonRetryableException expectedException = assertThrows(NonRetryableException.class, () -> {
            underTest.processMessage(payload);
        });

        verify(logger, times(2)).trace(anyString());
        verify(logger, times(1)).error(
                eq("An error occurred while attempting to extract the TransactionID:"), any(JsonProcessingException.class));

        verifyNoInteractions(repository);
        verifyNoInteractions(producer);

        assertThat(expectedException.getMessage(), is("Error extracting transaction ID from message"));
    }

    @Test
    void givenEmptyPayload_whenFetchingTransactionID_thenRaiseException() {
        Message<transaction> message = buildTransactionEmptyDataMessage();

        List<MonitorQueryDocument> documents = createQueryDocuments();
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(documents);

        underTest.processMessage(message.getPayload());

        verify(logger, times(1)).trace("processMessage(message=%s) method called.".formatted(message.getPayload()));
        verify(logger, times(1)).debug("Delete non-existent filing for company: [%s] received - attempting to match users".
                formatted(COMPANY_NUMBER));
        verify(repository, times(1)).findByCompanyNumber(COMPANY_NUMBER);

        verify(producer, times(documents.size())).sendMessage(any(filing.class));
    }


    private List<MonitorQueryDocument> createQueryDocuments() {
        MonitorQueryDocument doc1 = new MonitorQueryDocument();
        doc1.setCompanyNumber("00006400");
        doc1.setUserId("user-id-123");

        MonitorQueryDocument doc2 = new MonitorQueryDocument();
        doc2.setCompanyNumber("00006400");
        doc2.setUserId("user-id-128");

        return List.of(doc1, doc2);
    }
}

