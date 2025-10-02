package uk.gov.companieshouse.monitornotification.matcher.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.COMPANY_NAME;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.COMPANY_NUMBER;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.COMPANY_STATUS;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.buildFilingDeleteMessageWithBlankCompanyNumber;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.buildFilingDeleteMessageWithoutCompanyNumber;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.buildFilingDeleteMessageWithoutIsDelete;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.buildFilingUpdateMessage;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import monitor.filing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.ExternalLinksProperties;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.model.EmailDocument;
import uk.gov.companieshouse.monitornotification.matcher.service.CompanyService;
import uk.gov.companieshouse.monitornotification.matcher.service.EmailService;

@ExtendWith(MockitoExtension.class)
public class MessageProcessorTest {

    @Mock
    EmailService emailService;

    @Mock
    CompanyService companyService;

    ObjectMapper mapper;

    @Mock
    Logger logger;

    ExternalLinksProperties properties;

    MessageProcessor underTest;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();

        properties = new ExternalLinksProperties();
        properties.setChsUrl("https://chs-url.companieshouse.gov.uk");
        properties.setMonitorUrl("https://monitor-url.companieshouse.gov.uk");

        underTest = new MessageProcessor(emailService, companyService, mapper, logger, properties);
    }

    @Test
    void givenCompanyNumberMissing_whenMessageProcessed_thenProcessingTerminated() {
        Message<filing> message = buildFilingDeleteMessageWithoutCompanyNumber();
        filing payload = message.getPayload();

        underTest.processMessage(payload);

        verify(logger, times(3)).trace(anyString());
        verify(logger, times(1)).info("No company number was detected within the notification match payload. Processing aborted!");
        verify(logger, times(1)).debug(anyString());
        verifyNoInteractions(companyService);
        verifyNoInteractions(emailService);
    }

    @Test
    void givenCompanyNumberBlank_whenMessageProcessed_thenProcessingTerminated() {
        Message<filing> message = buildFilingDeleteMessageWithBlankCompanyNumber();
        filing payload = message.getPayload();

        underTest.processMessage(payload);

        verify(logger, times(3)).trace(anyString());
        verify(logger, times(1)).info("No company number was detected within the notification match payload. Processing aborted!");
        verify(logger, times(1)).debug(anyString());
        verifyNoInteractions(companyService);
        verifyNoInteractions(emailService);
    }

    @Test
    void givenCompanyNumberSupplied_whenCompanyNotExists_thenProcessingTerminated() {
        Message<filing> message = buildFilingUpdateMessage();
        filing payload = message.getPayload();

        when(companyService.findCompanyDetails(COMPANY_NUMBER)).thenReturn(Optional.empty());

        underTest.processMessage(payload);

        verify(logger, times(3)).trace(anyString());
        verify(logger, times(1)).info("No company details were found with company number: [%s]. Processing aborted!".formatted(COMPANY_NUMBER));
        verify(logger, times(1)).debug(anyString());
        verify(companyService, times(1)).findCompanyDetails(COMPANY_NUMBER);
        verifyNoInteractions(emailService);
    }

    @Test
    void givenIsDeleteMissing_whenMessageProcessed_thenProcessingCompletedWithUpdate() {
        Message<filing> message = buildFilingDeleteMessageWithoutIsDelete();
        filing payload = message.getPayload();

        CompanyDetails companyDetails = new CompanyDetails();
        companyDetails.setCompanyNumber(COMPANY_NUMBER);
        companyDetails.setCompanyName(COMPANY_NAME);
        companyDetails.setCompanyStatus(COMPANY_STATUS);

        when(companyService.findCompanyDetails(COMPANY_NUMBER)).thenReturn(Optional.of(companyDetails));

        underTest.processMessage(payload);

        verify(logger, times(6)).trace(anyString());
        verify(logger, times(1)).info("The message does not contain a valid is_delete field (defaulting to FALSE).");
        verify(logger, times(3)).debug(anyString());
        verify(companyService, times(1)).findCompanyDetails(COMPANY_NUMBER);
        verify(emailService, times(1)).saveMatch(any(EmailDocument.class));
        verify(emailService, times(1)).sendEmail(any(EmailDocument.class));
    }

    @Test
    void givenInvalidPayload_whenMessageProcessed_thenExceptionRaised() {
        Message<filing> message = buildFilingUpdateMessage();
        filing payload = message.getPayload();

        payload.setData("!nvalid json");

        NonRetryableException expectedException = assertThrows(NonRetryableException.class, () -> {
            underTest.processMessage(payload);
        });

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("An error occurred while attempting to extract the JsonNode: company_number"));
        assertThat(expectedException.getCause().getClass(), is(JsonParseException.class));
    }

}
