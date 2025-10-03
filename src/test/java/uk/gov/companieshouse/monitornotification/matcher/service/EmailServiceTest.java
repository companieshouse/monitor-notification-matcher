package uk.gov.companieshouse.monitornotification.matcher.service;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.USER_ID;
import static uk.gov.companieshouse.monitornotification.matcher.util.NotificationMatchTestUtils.buildValidEmailDocument;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.monitornotification.matcher.exception.NonRetryableException;
import uk.gov.companieshouse.monitornotification.matcher.model.EmailDocument;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    Logger logger;

    @Mock
    ObjectMapper mapper;

    EmailService underTest;

    @BeforeEach
    void setUp() {
        underTest = new EmailService(mapper, logger);
    }

    @Test
    public void givenValidDocument_whenSendEmailCalled_thenSuccess() {
        EmailDocument<Map<String, Object>> document = buildValidEmailDocument(TRUE);

        underTest.sendEmail(document, USER_ID);

        verify(logger, times(1)).trace(anyString());
    }


    @Test
    public void givenInvalidDocument_whenSaveMatchCalled_thenParseExceptionRaised() throws JsonProcessingException {
        EmailDocument<Map<String, Object>> document = buildValidEmailDocument(FALSE);
        when(mapper.writeValueAsString(document.getData())).thenThrow(JsonProcessingException.class);

        NonRetryableException expectedException = assertThrows(NonRetryableException.class, () -> {
            underTest.saveMatch(document, USER_ID);
        });

        verify(logger, times(1)).trace(anyString());
        verify(mapper, times(1)).writeValueAsString(document.getData());
        verify(logger, times(1)).error(anyString());

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("Failed to serialize email data"));
        assertThat(expectedException.getCause().getClass(), is(JsonProcessingException.class));
    }

    @Test
    public void givenInvalidDocument_whenSendEmailCalled_thenParseExceptionRaised() throws JsonProcessingException {
        EmailDocument<Map<String, Object>> document = buildValidEmailDocument(FALSE);
        when(mapper.writeValueAsString(document.getData())).thenThrow(JsonProcessingException.class);

        NonRetryableException expectedException = assertThrows(NonRetryableException.class, () -> {
            underTest.sendEmail(document, USER_ID);
        });

        verify(logger, times(1)).trace(anyString());
        verify(mapper, times(1)).writeValueAsString(document.getData());
        verify(logger, times(1)).error(anyString());

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getMessage(), is("Failed to serialize email data"));
        assertThat(expectedException.getCause().getClass(), is(JsonProcessingException.class));
    }
}
