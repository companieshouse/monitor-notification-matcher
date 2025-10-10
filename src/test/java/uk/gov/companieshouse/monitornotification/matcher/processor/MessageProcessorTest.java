package uk.gov.companieshouse.monitornotification.matcher.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.COMPANY_NAME;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.COMPANY_NUMBER;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.COMPANY_STATUS;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.buildFilingDeleteMessageWithBlankCompanyNumber;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.buildFilingDeleteMessageWithoutCompanyNumber;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.buildFilingDeleteMessageWithoutIsDelete;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.buildFilingUpdateMessage;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import monitor.filing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.api.chskafka.MessageSend;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.config.properties.ExternalLinksProperties;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.service.CompanyService;
import uk.gov.companieshouse.monitornotification.matcher.service.EmailService;
import uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchDataExtractor;

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
    NotificationMatchDataExtractor extractor;

    MessageProcessor underTest;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();

        properties = new ExternalLinksProperties();
        properties.setChsUrl("https://chs-url.companieshouse.gov.uk");
        properties.setMonitorUrl("https://monitor-url.companieshouse.gov.uk");

        extractor = new NotificationMatchDataExtractor(mapper, logger);

        underTest = new MessageProcessor(emailService, companyService, logger, properties, extractor);
    }

    @Test
    void givenCompanyNumberMissing_whenMessageProcessed_thenProcessingTerminated() {
        Message<filing> message = buildFilingDeleteMessageWithoutCompanyNumber();
        filing payload = message.getPayload();

        underTest.processMessage(payload);

        verify(logger, times(4)).trace(anyString());
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

        verify(logger, times(4)).trace(anyString());
        verify(logger, times(1)).info("No company number was detected within the notification match payload. Processing aborted!");
        verify(logger, times(0)).debug(anyString());
        verifyNoInteractions(companyService);
        verifyNoInteractions(emailService);
    }

    @Test
    void givenCompanyNumberSupplied_whenCompanyNotExists_thenProcessingTerminated() {
        Message<filing> message = buildFilingUpdateMessage();
        filing payload = message.getPayload();

        when(companyService.findCompanyDetails(COMPANY_NUMBER)).thenReturn(Optional.empty());

        underTest.processMessage(payload);

        verify(logger, times(4)).trace(anyString());
        verify(logger, times(1)).info("No company details were found with company number: [%s]. Processing aborted!".formatted(COMPANY_NUMBER));
        verify(logger, times(0)).debug(anyString());
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
        doNothing().when(emailService).saveMatch(any(MessageSend.class));
        when(emailService.sendEmail(any(MessageSend.class))).thenReturn(new ApiResponse<>(201, Map.of(), null));

        underTest.processMessage(payload);

        verify(logger, times(21)).trace(anyString());
        verify(logger, times(1)).info(anyString());
        verify(logger, times(2)).debug(anyString());
        verify(companyService, times(1)).findCompanyDetails(COMPANY_NUMBER);
        verify(emailService, times(1)).saveMatch(any(MessageSend.class));
        verify(emailService, times(1)).sendEmail(any(MessageSend.class));
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
        assertThat(expectedException.getMessage(), is("An error occurred while attempting to extract the JsonNode: data"));
        assertThat(expectedException.getCause().getClass(), is(JsonParseException.class));
    }

}
