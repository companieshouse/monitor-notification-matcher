package uk.gov.companieshouse.monitornotification.matcher.service;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.monitornotification.matcher.utils.NotificationMatchTestUtils.buildValidEmailDocument;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.MessageSend;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateMessageSendHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateMessageSendPost;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.repository.MonitorMatchesRepository;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    Supplier<InternalApiClient> supplier;

    @Mock
    MonitorMatchesRepository repository;

    @Mock
    Logger logger;

    @Mock
    ObjectMapper mapper;

    EmailService underTest;

    @BeforeEach
    void setUp() {
        underTest = new EmailService(supplier, repository, mapper, logger);
    }

    @Test
    void givenValidDocument_whenSendEmailCalled_thenSuccess() throws ApiErrorResponseException {
        InternalApiClient client = mock(InternalApiClient.class);
        HttpClient httpClient = mock(HttpClient.class);
        PrivateMessageSendHandler handler = mock(PrivateMessageSendHandler.class);
        PrivateMessageSendPost poster = mock(PrivateMessageSendPost.class);

        when(supplier.get()).thenReturn(client);
        when(client.getHttpClient()).thenReturn(httpClient);
        when(client.messageSendHandler()).thenReturn(handler);
        when(handler.postMessageSend(eq("/message-send"), any(MessageSend.class))).thenReturn(poster);
        when(poster.execute()).thenReturn(new ApiResponse<>(200, Map.of(), null));

        MessageSend document = buildValidEmailDocument(TRUE);

        underTest.sendEmail(document);

        verify(logger, times(1)).trace(anyString());
        verify(supplier, times(1)).get();

        verify(client, times(2)).getHttpClient();
        verify(httpClient, times(1)).setRequestId(anyString());
        verify(client, times(1)).messageSendHandler();
        verify(handler, times(1)).postMessageSend(eq("/message-send"), any(MessageSend.class));
        verify(poster, times(1)).execute();
    }

    @Test
    void givenValidDocument_whenSendEmailFails_thenRaiseException() throws ApiErrorResponseException {
        InternalApiClient client = mock(InternalApiClient.class);
        HttpClient httpClient = mock(HttpClient.class);
        PrivateMessageSendHandler handler = mock(PrivateMessageSendHandler.class);
        PrivateMessageSendPost poster = mock(PrivateMessageSendPost.class);

        ApiErrorResponseException exceptionToRaise = ApiErrorResponseException.fromHttpResponseException(
                new HttpResponseException.Builder(400, "Bad Request", new HttpHeaders()).build()
        );

        when(supplier.get()).thenReturn(client);
        when(client.getHttpClient()).thenReturn(httpClient);
        when(client.messageSendHandler()).thenReturn(handler);
        when(handler.postMessageSend(eq("/message-send"), any(MessageSend.class))).thenReturn(poster);
        when(poster.execute()).thenThrow(exceptionToRaise);

        MessageSend document = buildValidEmailDocument(TRUE);

        NonRetryableException expectedException = assertThrows(NonRetryableException.class, () -> {
            underTest.sendEmail(document);
        });

        verify(logger, times(1)).trace(anyString());
        verify(supplier, times(1)).get();

        verify(client, times(1)).getHttpClient();
        verify(httpClient, times(1)).setRequestId(anyString());
        verify(client, times(1)).messageSendHandler();
        verify(handler, times(1)).postMessageSend(eq("/message-send"), any(MessageSend.class));
        verify(poster, times(1)).execute();

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is(nullValue()));
        assertThat(expectedException.getCause().getClass(), is(ApiErrorResponseException.class));
    }


    @Test
    void givenValidDocument_whenSaveMatchCalled_thenSuccess() {
        MessageSend document = buildValidEmailDocument(TRUE);

        underTest.saveMatch(document);

        verify(logger, times(1)).trace(anyString());
    }

    @Test
    void givenInvalidDocument_whenSaveMatchCalled_thenParseExceptionRaised() throws JsonProcessingException {
        MessageSend document = buildValidEmailDocument(FALSE);
        when(mapper.writeValueAsString(document.getData())).thenThrow(JsonProcessingException.class);

        NonRetryableException expectedException = assertThrows(NonRetryableException.class, () -> {
            underTest.saveMatch(document);
        });

        verify(logger, times(1)).trace(anyString());
        verify(mapper, times(1)).writeValueAsString(document.getData());
        verify(logger, times(1)).error(anyString());

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("Failed to serialize email data"));
        assertThat(expectedException.getCause().getClass(), is(JsonProcessingException.class));
    }

}
